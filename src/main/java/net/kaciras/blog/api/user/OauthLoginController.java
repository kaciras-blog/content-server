package net.kaciras.blog.api.user;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/connect")
@RequiredArgsConstructor
public class OauthLoginController {

	private static final String ClientID = "08878e62e2f3cb5be51b";
	private static final String CLIENT_SECRET = "04b1aebe5770a4458bfa17d79bc5cd1988dcba45";

	private final RestTemplate restTemplate;

	@GetMapping("/github")
	public ResponseEntity<Void> github() {
		var authUri = UriComponentsBuilder.fromUriString("https://github.com/login/oauth/authorize")
				.queryParam("client_id", ClientID)
				.queryParam("scope", "read:user")
				.queryParam("state", UUID.randomUUID().toString())
				.build().toUri();
		return ResponseEntity.status(302).location(authUri).build();
	}

	// 用户在Github上确认授权后将跳转到此端点上
	// Github的 access_token 不过期
	@GetMapping("/github/callback")
	public void callback(@RequestParam String code) {
		var authUri = UriComponentsBuilder.fromUriString("https://github.com/login/oauth/access_token")
				.queryParam("client_id", ClientID)
				.queryParam("client_secret", CLIENT_SECRET)
				.queryParam("code", code)
				.build().toUri();
		var res = restTemplate.postForObject(authUri, null, AccessTokenEntity.class);

		var resp = restTemplate.getForObject("https://api.github.com/user?access_token=" + res.access_token, Map.class);
		System.out.println(resp);
	}

	@Setter
	private static class AccessTokenEntity {
		public String access_token;
		public String scope;
		public String token_type;
	}

	private static final String GOOGLE_ID = "834515199449-58mpslgflbmhsvj1ovabhili352s6sc5.apps.googleusercontent.com";
	private static final String GOOGLE_SECRET = "H-MqP5dro6-KsJPqj1egF7vA";

	@GetMapping("/google")
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
	@GetMapping("/google/callback")
	public void googleCallback(@RequestParam String code) {

		// www-form-encoding
		var data = new HashMap<String, String>();
		data.put("client_id", GOOGLE_ID);
		data.put("client_secret", GOOGLE_SECRET);
		data.put("code", code);
		data.put("grant_type", "authorization_code");

		var res = restTemplate.postForObject("https://www.googleapis.com/oauth2/v4/token", data, GoogleTokenResp.class);

		var uu = UriComponentsBuilder.fromUriString("https://www.googleapis.com/oauth2/v2/userinfo")
				.queryParam("fields", "id,name,picture")
				.queryParam("key", res.access_token)
				.build().toUri();
		var resp = restTemplate.getForObject(uu, Map.class);
		System.out.println(resp);
	}

	@Setter
	private static class GoogleTokenResp {
		public String access_token;
		public String refresh_token;
		public String token_type;
		public int expires_in; // 秒,默认1小时
	}
}
