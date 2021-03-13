package com.kaciras.blog.api.account.oauth2;

import com.kaciras.blog.api.account.AuthType;
import com.kaciras.blog.api.account.SessionService;
import com.kaciras.blog.api.user.User;
import com.kaciras.blog.api.user.UserRepository;
import com.kaciras.blog.infra.RequestUtils;
import com.kaciras.blog.infra.codec.ImageReference;
import com.kaciras.blog.infra.exception.PermissionException;
import com.kaciras.blog.infra.exception.RequestArgumentException;
import com.kaciras.blog.infra.exception.ResourceDeletedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 处理 OAuth2 登录的控制器，OAuth2 有固定的流程所以所以统一在此处理。
 * 也是在 OAuth2 流程中，用于本系统和第一方用户通信的类。
 *
 * <h2>为何不用 Spring-Security-OAuth </h2>
 * 虽然 Spring 有这功能，但因为 OAuth2 流程很简单所以自己写一遍学习一下。
 *
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.1">RFC6749</a>
 * @see org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/oauth2")
class OAuth2Controller {

	/** 登录会话的过期时间 */
	private static final Duration TIMEOUT = Duration.ofMinutes(10);

	/** OAuth2 上下文存储在 HttpSession 的这个属性里 */
	private static final String SESSION_KEY = "OA";

	private final SessionService sessionService;
	private final OAuth2DAO oAuth2DAO;
	private final UserRepository userRepository;

	private final Clock clock;

	private Map<String, OAuth2Client> clientMap = Collections.emptyMap();

	@Value("${app.origin}")
	private String origin;

	// 没有 bean 时注入 null 而不是空集合？
	@Autowired(required = false)
	void initClientMap(Collection<OAuth2Client> beans) {
		clientMap = beans.stream()
				.collect(Collectors.toMap(b -> b.authType().name().toLowerCase(), Function.identity()));
	}

	/**
	 * OAuth 认证第一步，开始登录会话，生成跳转链接，将用户重定向到第三方授权页面。
	 *
	 * @param type    第三方名称
	 * @param request 请求对象
	 */
	@GetMapping("/connect/{type}")
	public ResponseEntity<Void> redirect(HttpServletRequest request, @PathVariable String type) {
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
		var state = UUID.randomUUID().toString();
		var returnUrl = request.getParameter("ret");
		var ctx = new OAuth2Context(type, state, returnUrl, clock.instant());
		request.getSession(true).setAttribute(SESSION_KEY, ctx);

		// request.getRequestURL() 不包含参数和 hash 部分
		var redirect = UriComponentsBuilder
				.fromUriString(request.getRequestURL().toString())
				.replacePath("/oauth2/callback");

		var authUri = client.authenticateUri()
				.queryParam("state", ctx.state)
				.queryParam("redirect_uri", redirect.toUriString())
				.build();

		return ResponseEntity.status(302).location(authUri.toUri()).build();
	}

	/**
	 * OAuth 认证第二步，用户在第三方服务上确认授权，然后向本服务提供授权码。
	 * 本服务需要根据使用该授权码来从第三方服务获取必要的信息以完成登录。
	 *
	 * @param code     返回的认证码
	 * @param request  请求对象
	 * @param response 响应对象
	 */
	@GetMapping("/callback")
	public ResponseEntity<Void> callback(@RequestParam String code,
										 @RequestParam String state,
										 HttpServletRequest request,
										 HttpServletResponse response) throws Exception {
		var authSession = retrieveOAuthSession(request);
		var client = clientMap.get(authSession.provider);

		// 从第三方读取用户信息
		var authorization = new AuthorizeRequest(code, state, request.getRequestURL().toString());
		var info = client.authorize(authorization);

		// 查询出在本系统里对应的用户，并设置会话属性（登录）
		var localId = getLocalId(info, request, client.authType());
		sessionService.putUser(request, response, localId, true);

		// 没有跳转，可能不是从页面过来的请求，但也算正常请求
		if (authSession.returnUri == null) {
			return ResponseEntity.ok().build();
		}

		// 固定域名和协议，以防跳转到其他网站
		var redirect = UriComponentsBuilder
				.fromUriString(authSession.returnUri)
				.uri(URI.create(origin))
				.build();

		return ResponseEntity.status(302).location(redirect.toUri()).build();
	}

	/**
	 * 获取请求用户保存的 OAuth2 会话，同时会做必要的安全检查。
	 * 会话是一次性的，一旦获取就会从存储中删除。
	 *
	 * @param request 请求对象
	 * @return OAuth2 会话
	 * @throws ResourceDeletedException 如果认证会话过期了
	 * @throws PermissionException      如果存在安全问题
	 */
	private OAuth2Context retrieveOAuthSession(HttpServletRequest request) {
		var session = request.getSession(true);
		var ctx = (OAuth2Context) session.getAttribute(SESSION_KEY);
		var valid = clock.instant().minus(TIMEOUT);

		if (ctx == null || ctx.time.isBefore(valid)) {
			throw new PermissionException("认证请求无效或已过期，请重新登录");
		}

		// 会话是一次性的，使用后立即删除。
		session.removeAttribute(SESSION_KEY);

		// 检查请求中的 state 字段与会话中的是否一致，不同则终止并返回错误信息。
		var state = request.getParameter("state");
		if (ctx.state.equals(state)) {
			return ctx;
		}
		throw new RequestArgumentException("参数错误，您可能点击了不安全的链接");
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
	int getLocalId(UserProfile profile, HttpServletRequest request, AuthType authType) {
		var localId = oAuth2DAO.select(profile.id(), authType);
		if (localId != null) {
			return localId;
		}

		var user = new User();
		user.setAvatar(ImageReference.parse("akalin.jpg"));
		user.setName(profile.name());
		user.setEmail(profile.email());
		user.setAuth(authType);
		user.setCreateIP(RequestUtils.addressFrom(request));

		userRepository.add(user);
		oAuth2DAO.insert(profile.id(), authType, user.getId());
		return user.getId();
	}
}
