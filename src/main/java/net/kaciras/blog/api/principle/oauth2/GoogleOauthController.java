package net.kaciras.blog.api.principle.oauth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/connect/google")
public class GoogleOauthController {

	private static final String GOOGLE_ID = "834515199449-f1ia0v8b3fa5hnvuimrradoetulc5nvi.apps.googleusercontent.com";
	private static final String GOOGLE_SECRET = "jT7m_0N5zS-QzuzwGKYnYuFo";

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;

	@GetMapping
	public ResponseEntity<Void> google() {
		var authUri = UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/auth")
				.queryParam("client_id", GOOGLE_ID)
				.queryParam("scope", "https://www.googleapis.com/auth/userinfo.profile")
				.queryParam("redirect_uri", "https://localhost:2375/connect/google/callback")
				.queryParam("state", UUID.randomUUID().toString())
				.queryParam("response_type", "code")
				.queryParam("access_type", "offline")
				.build().toUri();
		return ResponseEntity.status(302).location(authUri).build();
	}

	// 对本应用来说，仅需获取一次就够了，没必要刷新 access_token
	@GetMapping("/callback")
	public ResponseEntity<Void> googleCallback(@RequestParam String code) throws IOException, InterruptedException {
		var formParams = UriComponentsBuilder.newInstance()
				.queryParam("client_id", GOOGLE_ID)
				.queryParam("client_secret", GOOGLE_SECRET)
				.queryParam("code", code)
				.queryParam("redirect_uri", "https://localhost:2375/connect/google/callback")
				.queryParam("grant_type", "authorization_code");

		var request = HttpRequest
				.newBuilder(URI.create("https://www.googleapis.com/oauth2/v4/token"))
				.POST(BodyPublishers.ofString(formParams.build().getQuery()))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.build();

		var res = httpClient.send(request, BodyHandlers.ofInputStream());
		if (res.statusCode() != 200) {
			throw new Error("Oauth Error" + res.body());
		}
		var tokenEntity = objectMapper.readValue(res.body(), GoogleTokenResp.class);
		var profile = getUserProfile(tokenEntity.access_token);

		// todo
		return ResponseEntity.status(200).build();
	}

	private UserInfo getUserProfile(String accessToken) throws IOException, InterruptedException {
		var uu = UriComponentsBuilder
				.fromUriString("https://www.googleapis.com/oauth2/v2/userinfo")
				.queryParam("fields", "id,name,picture")
				.build().toUri();

		var request = HttpRequest.newBuilder(uu)
				.header("Authorization", "Bearer " + accessToken)
				.build();
		var res = httpClient.send(request, BodyHandlers.ofInputStream());
		return objectMapper.readValue(res.body(), UserInfo.class);
	}

	@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
	private static final class GoogleTokenResp {
		public final String access_token;
		public final String refresh_token;
		public final String token_type;
		public final int expires_in; // 秒,默认1小时
	}

	@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
	private static final class UserInfo {
		public long id;
		public String name;
		public String picture;
	}
}
