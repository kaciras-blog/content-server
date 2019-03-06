package net.kaciras.blog.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kaciras.blog.api.MultiPartBodyPublisher;
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
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/connect/google")
public class GoogleOauthController {

	private static final String GOOGLE_ID = "834515199449-58mpslgflbmhsvj1ovabhili352s6sc5.apps.googleusercontent.com";
	private static final String GOOGLE_SECRET = "H-MqP5dro6-KsJPqj1egF7vA";

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;

	@GetMapping
	public ResponseEntity<Void> google() {
		var authUri = UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
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
	public void googleCallback(@RequestParam String code) throws IOException, InterruptedException {
		var publisher = new MultiPartBodyPublisher()
				.addPart("client_id", GOOGLE_ID)
				.addPart("client_secret", GOOGLE_SECRET)
				.addPart("code", code)
				.addPart("grant_type", "authorization_code");

		var request = HttpRequest
				.newBuilder(URI.create("https://www.googleapis.com/oauth2/v4/token"))
				.POST(publisher.build())
				.header("Content-Type", "multipart/form-data; boundary=" + publisher.getBoundary())
				.build();

		var res = httpClient.send(request, BodyHandlers.ofInputStream());
		var tokenEntity = objectMapper.readValue(res.body(), GoogleTokenResp.class);

		var profile = getUserProfile(tokenEntity.access_token);

	}

	private Map<String, Object> getUserProfile(String accessToken) throws IOException, InterruptedException {
		var uu = UriComponentsBuilder
				.fromUriString("https://www.googleapis.com/oauth2/v2/userinfo")
				.queryParam("key", accessToken)
				.queryParam("fields", "id,name,picture")
				.build().toUri();

		var request = HttpRequest.newBuilder(uu).GET().build();
		var res = httpClient.send(request, BodyHandlers.ofInputStream());

		return objectMapper.readValue(res.body(), Map.class);
	}

	@Setter
	private static class GoogleTokenResp {
		public String access_token;
		public String refresh_token;
		public String token_type;
		public int expires_in; // 秒,默认1小时
	}
}
