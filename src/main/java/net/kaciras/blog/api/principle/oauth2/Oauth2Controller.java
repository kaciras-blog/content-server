package net.kaciras.blog.api.principle.oauth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.Utils;
import net.kaciras.blog.api.principle.AuthType;
import net.kaciras.blog.api.principle.SessionService;
import net.kaciras.blog.api.principle.oauth2.Oauth2Client.UserInfo;
import net.kaciras.blog.api.user.UserManager;
import net.kaciras.blog.infrastructure.func.Lambdas;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/connect")
public class Oauth2Controller {

	private static final String OAUTH_STATE = "oas:";

	private final SessionService sessionService;
	private final OauthDAO oauthDAO;
	private final UserManager userManager;

	private final RedisTemplate<String, byte[]> redisTemplate;
	private final ObjectMapper objectMapper;

	private Map<String, Oauth2Client> clientMap;

	@Autowired
	void initClientMap(Collection<Oauth2Client> beans) {
		clientMap = beans.stream()
				.collect(Collectors.toMap(b -> b.authType().name().toLowerCase(), Lambdas.keepIntact()));
	}

	@GetMapping("/{type}")
	public ResponseEntity<Void> redirect(@PathVariable String type, HttpServletRequest request) throws JsonProcessingException {
		var client = clientMap.get(type);
		if (client == null) {
			return ResponseEntity.badRequest().build();
		}

		// 生成随机state参数，与返回页面一起保存到Redis里
		var authSession = new OauthSession(UUID.randomUUID().toString(), request.getParameter("ret"));
		var key = OAUTH_STATE + request.getSession(true).getId();
		redisTemplate.opsForValue()
				.set(key, objectMapper.writeValueAsBytes(authSession), Duration.ofMinutes(10));

		var redirect = UriComponentsBuilder
				.fromUriString(request.getRequestURL().toString())
				.replaceQuery(null)
				.path("/callback");

		var authUri = client.authUri()
				.queryParam("state", authSession.state)
				.queryParam("redirect_uri", redirect.toUriString())
				.build().toUri();

		return ResponseEntity.status(302).location(authUri).build();
	}

	@GetMapping("/{type}/callback")
	public ResponseEntity<?> callback(@PathVariable String type, HttpServletRequest request, HttpServletResponse response) throws Exception {
		var client = clientMap.get(type);
		if (client == null) {
			return ResponseEntity.badRequest().build();
		}

		// 以下两步验证state，防止CSRF攻击
		var key = OAUTH_STATE + request.getSession(true).getId();
		var record = redisTemplate.opsForValue().get(key);
		if (record == null) {
			return ResponseEntity.status(410).body("认证请求无效或已过期，请重试");
		}

		var oauthSession = objectMapper.readValue(record, OauthSession.class);
		redisTemplate.delete(key);
		var state = request.getParameter("state");
		if (!oauthSession.state.equals(state)) {
			return ResponseEntity.status(403).body("认证参数错误，您可能点击了不安全的链接");
		}

		var currentUri = UriComponentsBuilder
				.fromUriString(request.getRequestURL().toString())
				.replaceQuery(null).toUriString();

		var context = new AuthContext(request.getParameter("code"), currentUri, state);

		var info = client.getUserInfo(context);
		var localId = getLocalId(info, request, client.authType());
		sessionService.putUser(request, response, localId, true);

		// 没有跳转，可能不是从页面过来的请求，但也算正常请求。
		if (oauthSession.returnUri == null) {
			return ResponseEntity.ok().build();
		}

		// 强制域名，以防防跳转到其他网站
		var ret = UriComponentsBuilder
				.fromUriString(oauthSession.returnUri)
				.scheme("https").host("localhost");

		return ResponseEntity.status(302).location(ret.build().toUri()).build();
	}

	@Transactional
	protected int getLocalId(UserInfo profile, HttpServletRequest request, AuthType authType) {
		var localId = oauthDAO.select(profile.id(), authType);

		if (localId != null) {
			return localId;
		}
		var regIP = Utils.AddressFromRequest(request);
		var newId = userManager.createNew(profile.name(), authType, regIP);
		oauthDAO.insert(profile.id(), authType, newId);

		return newId;
	}

	@AllArgsConstructor(onConstructor_ = @JsonCreator)
	private static final class OauthSession {
		public String state;
		public String returnUri;
	}
}
