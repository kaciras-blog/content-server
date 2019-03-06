package net.kaciras.blog.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/connect/github")
public class GithubOauthController {

	private static final String ClientID = "08878e62e2f3cb5be51b";
	private static final String CLIENT_SECRET = "04b1aebe5770a4458bfa17d79bc5cd1988dcba45";

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;

	@GetMapping
	public ResponseEntity<Void> github() {
		var authUri = UriComponentsBuilder
				.fromUriString("https://github.com/login/oauth/authorize")
				.queryParam("client_id", ClientID)
				.queryParam("scope", "read:user")
				.queryParam("state", UUID.randomUUID().toString())
				.build().toUri();
		return ResponseEntity.status(302).location(authUri).build();
	}

	// 用户在Github上确认授权后将跳转到此端点上
	// Github的 access_token 不过期
	@GetMapping("/callback")
	public ResponseEntity<Void> callback(@RequestParam String code) throws IOException, InterruptedException {
		var token = getAccessToken(code).access_token;
		var profile = getUserProfile(token);



		return ResponseEntity.status(200).build();
	}

	private AccessTokenEntity getAccessToken(String code) throws IOException, InterruptedException {
		var authUri = UriComponentsBuilder
				.fromUriString("https://github.com/login/oauth/access_token")
				.queryParam("client_id", ClientID)
				.queryParam("client_secret", CLIENT_SECRET)
				.queryParam("code", code)
				.build().toUri();

		var request = HttpRequest.newBuilder().uri(authUri).build();
		var res = httpClient.send(request, BodyHandlers.ofInputStream());

		if (res.statusCode() != 200) {
			throw new IOException("Oauth获取AccessToken失败，返回码：" + res.statusCode());
		}
		return objectMapper.readValue(res.body(), AccessTokenEntity.class);
	}

	private UserProfile getUserProfile(String accessToken) throws IOException, InterruptedException {
		var request = HttpRequest
				.newBuilder(URI.create("https://api.github.com/user"))
				.header("Authorization", "token " + accessToken)
				.build();
		var res = httpClient.send(request, BodyHandlers.ofInputStream());

		if (res.statusCode() != 200) {
			throw new IOException("获取用户信息失败，返回码：" + res.statusCode());
		}
		return objectMapper.readValue(res.body(), UserProfile.class);
	}

	@Setter
	private static final class AccessTokenEntity {
		public String access_token;
		public String scope;
		public String token_type;
	}

	@Setter
	private static final class UserProfile {
		public String name;
		public int id;
		public String avatar_url;
	}
}
