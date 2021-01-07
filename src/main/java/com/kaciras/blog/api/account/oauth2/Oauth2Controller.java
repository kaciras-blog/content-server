package com.kaciras.blog.api.account.oauth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.RedisKeys;
import com.kaciras.blog.api.Utils;
import com.kaciras.blog.api.account.AuthType;
import com.kaciras.blog.api.account.SessionService;
import com.kaciras.blog.api.user.UserManager;
import com.kaciras.blog.infra.exception.PermissionException;
import com.kaciras.blog.infra.exception.ResourceDeletedException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 处理 OAuth2 登录的控制器，OAuth2 有固定的流程所以所以统一在此处理。
 * <p>
 * 虽然 Spring 有这功能，但因为 OAuth2 流程很简单所以自己写一遍学习一下。
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/connect/{type}")
public class Oauth2Controller {

	private final SessionService sessionService;
	private final OAuth2DAO oAuth2DAO;
	private final UserManager userManager;

	private final RedisTemplate<String, byte[]> redisTemplate;
	private final ObjectMapper objectMapper;

	private Map<String, Oauth2Client> clientMap = Collections.emptyMap();

	@Value("${app.origin}")
	private String origin;

	// 没有 bean 时注入 null 而不是空集合？
	@Autowired(required = false)
	private void initClientMap(Collection<Oauth2Client> beans) {
		clientMap = beans.stream()
				.collect(Collectors.toMap(b -> b.authType().name().toLowerCase(), b -> b));
	}

	/**
	 * OAuth 认证第一步，开始登录会话，生成跳转链接，将用户重定向到第三方授权页面。
	 *
	 * @param type    第三方名称
	 * @param request 请求对象
	 */
	@GetMapping
	public ResponseEntity<Void> redirect(
			@PathVariable String type,
			HttpServletRequest request) throws JsonProcessingException {

		var client = clientMap.get(type);
		if (client == null) {
			return ResponseEntity.notFound().build();
		}

		/*
		 * 生成随机 state 参数防止 CSRF，它将与返回页面一起保存到 Redis 里。
		 *
		 * 【攻击场景】
		 * 攻击者首先点击此链接并认证，拿到 code 后不跳转，而是将跳转的连接发给受害者，
		 * 受害者点击该跳转链接后将使用攻击者的 code 进行登录。
		 * 这导致受害者登录了攻击者的账号，攻击者可以诱骗其充值或填写敏感信息。
		 *
		 * 【如何防止】
		 * 加入 state 参数后，它将在跳转链接里被带上。state 值与会话相关联，攻击者无法修改受害者的 Cookie，
		 * 所以他的 state 与受害者没有关联（查询不到），这样可以验证登录跳转是否是同一人。
		 */
		var oauthSession = new OauthSession(UUID.randomUUID().toString(), request.getParameter("ret"));
		saveOAuthSession(request.getSession(true).getId(), oauthSession);

		// 【注意】request.getRequestURL() 不包含参数和hash部分
		var redirect = UriComponentsBuilder
				.fromUriString(request.getRequestURL().toString())
				.path("/callback");

		var authUri = client.authUri()
				.queryParam("state", oauthSession.state)
				.queryParam("redirect_uri", redirect.toUriString())
				.build().toUri();

		return ResponseEntity.status(302).location(authUri).build();
	}

	/**
	 * OAuth 认证第二步，用户在第三方服务上确认授权，然后向本服务提供授权码。
	 * 本服务需要根据使用该授权码来从第三方服务获取必要的信息以完成登录。
	 *
	 * @param type     第三方名称
	 * @param request  请求对象
	 * @param response 响应对象
	 */
	@GetMapping("/callback")
	public ResponseEntity<?> callback(@PathVariable String type,
									  HttpServletRequest request,
									  HttpServletResponse response) throws Exception {

		var client = clientMap.get(type);
		if (client == null) {
			return ResponseEntity.notFound().build();
		}

		// 获取会话，并检查state字段
		var oauthSession = retrieveOAuthSession(request);

		// 从第三方服务读取用户信息
		var code = request.getParameter("code");
		var context = new OAuth2Context(code, request.getRequestURL().toString(), oauthSession.state);
		var info = client.getUserInfo(context);

		// 查询出在本系统里对应的用户，并设置会话属性（登录）
		var localId = getLocalId(info, request, client.authType());
		sessionService.putUser(request, response, localId, true);

		// 没有跳转，可能不是从页面过来的请求，但也算正常请求
		if (oauthSession.returnUri == null) {
			return ResponseEntity.ok().build();
		}

		// 固定域名和协议，以防跳转到其他网站
		var redirect = UriComponentsBuilder
				.fromUriString(oauthSession.returnUri)
				.uri(URI.create(origin))
				.build().toUri();

		return ResponseEntity.status(302).location(redirect).build();
	}

	/**
	 * 保存临时的 OAuth2 会话，会话有一个较短的过期时间。
	 *
	 * @param id      用户会话的ID
	 * @param session OAuth2会话
	 */
	private void saveOAuthSession(String id, OauthSession session) throws JsonProcessingException {
		var key = RedisKeys.OauthSession.of(id);
		var value = objectMapper.writeValueAsBytes(session);
		redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(10));
	}

	/**
	 * 获取请求用户保存的 OAuth2 会话，同时会做必要的安全检查。
	 *
	 * @param request 请求对象
	 * @return 认证会话
	 * @throws ResourceDeletedException 如果认证会话过期了
	 * @throws PermissionException      如果存在安全问题
	 */
	private OauthSession retrieveOAuthSession(HttpServletRequest request) throws IOException {
		var key = RedisKeys.OauthSession.of(request.getSession(true).getId());
		var record = redisTemplate.opsForValue().get(key);
		if (record == null) {
			throw new ResourceDeletedException("认证请求无效或已过期，请重新登录");
		}

		// 会话是一次性的，使用后立即删除。
		redisTemplate.unlink(key);
		var oauthSession = objectMapper.readValue(record, OauthSession.class);

		// 检查请求中的 state 字段与会话中的是否一致，不同则终止并返回错误信息。
		var state = request.getParameter("state");
		if (oauthSession.state.equals(state)) {
			return oauthSession;
		}
		throw new PermissionException("参数错误，您可能点击了不安全的链接，或遭到了钓鱼攻击");
	}

	/**
	 * 根据第三方用户ID和类型从数据库里查询本地用户ID。
	 *
	 * @param profile  第三方用户信息
	 * @param request  请求对象
	 * @param authType 第三方类型
	 * @return 本地用户的ID，如果不存在则会创建
	 */
	@Transactional
	protected int getLocalId(Oauth2Client.UserInfo profile, HttpServletRequest request, AuthType authType) {
		var localId = oAuth2DAO.select(profile.id(), authType);

		if (localId != null) {
			return localId;
		}
		var regIP = Utils.addressFromRequest(request);
		var newId = userManager.createNew(profile.name(), authType, regIP);
		oAuth2DAO.insert(profile.id(), authType, newId);

		return newId;
	}

	@AllArgsConstructor(onConstructor_ = @JsonCreator)
	private static final class OauthSession {
		public final String state;
		public final String returnUri;
	}
}
