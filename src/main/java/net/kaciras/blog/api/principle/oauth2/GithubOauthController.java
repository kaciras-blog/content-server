package net.kaciras.blog.api.principle.oauth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.Utils;
import net.kaciras.blog.api.principle.AuthType;
import net.kaciras.blog.api.principle.SessionService;
import net.kaciras.blog.api.user.UserManager;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/connect/github")
public class GithubOauthController {

	private static final String ClientID = "08878e62e2f3cb5be51b";
	private static final String CLIENT_SECRET = "04b1aebe5770a4458bfa17d79bc5cd1988dcba45";

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;

	private final OauthDAO oauthDAO;
	private final UserManager userManager;
	private final SessionService sessionService;

	@GetMapping
	public ResponseEntity<Void> github(@RequestParam String ret) {
		var authUri = UriComponentsBuilder
				.fromUriString("https://github.com/login/oauth/authorize")
				.queryParam("client_id", ClientID)
				.queryParam("scope", "read:user")
				.queryParam("state", UUID.randomUUID().toString())
				.queryParam("redirect_uri", "https://localhost:2375/connect/github/callback/?ret=" + ret)
				.build().toUri();
		return ResponseEntity.status(302).location(authUri).build();
	}

	// Github的 access_token 不过期
	@GetMapping("/callback")
	public ResponseEntity<Void> callback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		var token = getAccessToken(request.getParameter("code"), request.getParameter("state"));
		var id = getLocalId(getUserProfile(token.access_token), request);
		sessionService.putUser(request, response, id, true);

		// 返回跳转必须验证域名，防止跳转到其他网站
		var retParam = Optional
				.ofNullable(request.getParameter("ret"))
				.orElse("/");
		var returnUri = UriComponentsBuilder.fromUriString(retParam);
		returnUri.scheme("https").host("localhost");
		return ResponseEntity.status(302).location(returnUri.build().toUri()).build();
	}

	@Transactional
	protected int getLocalId(UserProfile profile, HttpServletRequest request) {
		var localId = oauthDAO.select(profile.id, AuthType.Github);

		if (localId != null) {
			return localId;
		}

		var regIP = Utils.AddressFromRequest(request);
		var newId = userManager.createNew(profile.login, AuthType.Github, regIP);
		oauthDAO.insert(profile.id, AuthType.Github, newId);
		return newId;
	}

	private AccessTokenEntity getAccessToken(String code, String state) throws IOException, InterruptedException {
		var authUri = UriComponentsBuilder
				.fromUriString("https://github.com/login/oauth/access_token")
				.queryParam("client_id", ClientID)
				.queryParam("client_secret", CLIENT_SECRET)
				.queryParam("code", code)
				.queryParam("state", state)
				.build().toUri();

		var request = HttpRequest.newBuilder(authUri)
				.header("Accept", "application/json")
				.build();

		var res = httpClient.send(request, BodyHandlers.ofInputStream());
		if (res.statusCode() != 200) {
			throw new IOException("Oauth获取AccessToken失败，返回码：" + res.statusCode());
		}
		try (var input = res.body()) {
			return objectMapper.readValue(input, AccessTokenEntity.class);
		}
	}

	private UserProfile getUserProfile(String accessToken) throws IOException, InterruptedException {
		var request = HttpRequest
				.newBuilder(URI.create("https://api.github.com/user"))
				.header("Accept", "application/json")
				.header("Authorization", "token " + accessToken)
				.build();
		var res = httpClient.send(request, BodyHandlers.ofInputStream());

		if (res.statusCode() != 200) {
			throw new IOException("获取用户信息失败，返回码：" + res.statusCode());
		}
		try (var input = res.body()) {
			return objectMapper.readValue(input, UserProfile.class);
		}
	}

	@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
	private static final class AccessTokenEntity {
		public final String access_token;
		public final String scope;
		public final String token_type;
	}

	@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
	private static final class UserProfile {
		public final String login;
		public final String name;
		public final long id;
		public final String avatar_url;
	}
}
