package com.kaciras.blog.api.principal.oauth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.RedisKeys;
import com.kaciras.blog.api.Utils;
import com.kaciras.blog.api.principal.AuthType;
import com.kaciras.blog.api.principal.SessionService;
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
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/connect/{type}")
public class Oauth2Controller {

	private final SessionService sessionService;
	private final OAuth2DAO oAuth2DAO;
	private final UserManager userManager;

	private final RedisTemplate<String, byte[]> redisTemplate;
	private final ObjectMapper objectMapper;

	private Map<String, Oauth2Client> clientMap;

	@Value("${kaciras.oauth2.www-host}")
	private String wwwHost;

	@Autowired
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
	public ResponseEntity<Void> redirect(@PathVariable String type, HttpServletRequest request) throws JsonProcessingException {
		var client = clientMap.get(type);
		if (client == null) {
			return ResponseEntity.notFound().build();
		}

		/*
		 * 生成随机state参数，与返回页面一起保存到Redis里。
		 * state参数用于防止CSRF：
		 *
		 *     攻击者首先点击此链接并认证，拿到code后不跳转，而是将跳转的连接发给受害者，受害者
		 *     点击该跳转链接后将使用攻击者的code进行登录。这导致受害者登录了攻击者的账号，攻击者
		 *     可以诱骗受害者充值或填写敏感信息。
		 *
		 *     加入state参数后，它将在跳转链接里被带上。state值与会话相关联，攻击者无法修改受害者
		 *     的Cookie，所以他的state与受害者没有关联（查询不到），这样可以验证登录跳转是否是同
		 *     一人的。
		 */
		var authSession = new OauthSession(UUID.randomUUID().toString(), request.getParameter("ret"));
		var key = RedisKeys.OauthSession.of(request.getSession(true).getId());
		redisTemplate.opsForValue()
				.set(key, objectMapper.writeValueAsBytes(authSession), Duration.ofMinutes(10));

		// 【注意】request.getRequestURL() 不包含参数和hash部分
		var redirect = UriComponentsBuilder
				.fromUriString(request.getRequestURL().toString())
				.path("/callback");

		var authUri = client.authUri()
				.queryParam("state", authSession.state)
				.queryParam("redirect_uri", redirect.toUriString())
				.build().toUri();

		return ResponseEntity.status(302).location(authUri).build();
	}

	/**
	 * OAuth 认证第二步，用户在第三方服务上确认授权，带着授权码code返回到该请求。
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
				.scheme("https")
				.host(wwwHost)
				.build().toUri();

		return ResponseEntity.status(302).location(redirect).build();
	}

	/**
	 * 根据请求获取保存的认证会话，同时会检查请求中的 state 字段与会话中的是否
	 * 一致，如果不一致则抛出异常中止认证。
	 *
	 * @param request 请求对象
	 * @return 认证会话
	 * @throws ResourceDeletedException 如果认证会话过期了
	 * @throws PermissionException      如果会话中的state与请求中的不同
	 */
	private OauthSession retrieveOAuthSession(HttpServletRequest request) throws IOException {
		var key = RedisKeys.OauthSession.of(request.getSession(true).getId());
		var record = redisTemplate.opsForValue().get(key);
		if (record == null) {
			throw new ResourceDeletedException("认证请求无效或已过期，请重新登录");
		}

		redisTemplate.unlink(key);
		var oauthSession = objectMapper.readValue(record, OauthSession.class);

		var state = request.getParameter("state");
		if (!oauthSession.state.equals(state)) {
			throw new PermissionException("参数错误，您可能点击了不安全的链接，或遭到了钓鱼攻击");
		}
		return oauthSession;
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
		public String state;
		public String returnUri;
	}
}
