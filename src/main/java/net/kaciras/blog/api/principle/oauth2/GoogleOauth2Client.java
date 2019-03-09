package net.kaciras.blog.api.principle.oauth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.principle.AuthType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

@RequiredArgsConstructor
@Component
public class GoogleOauth2Client implements Oauth2Client {

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;

	@Value("${kaciras.oauth.google.client-id}")
	private String clientId;

	@Value("${kaciras.oauth.google.client-secret}")
	private String clientSecret;

	@Override
	public AuthType authType() {
		return AuthType.Google;
	}

	@Override
	public UriComponentsBuilder authUri() {
		return UriComponentsBuilder
				.fromUriString("https://accounts.google.com/o/oauth2/auth")
				.queryParam("client_id", clientId)
				.queryParam("scope", "https://www.googleapis.com/auth/userinfo.profile")
				.queryParam("response_type", "code")
				.queryParam("access_type", "offline");
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public UserInfo getUserInfo(OAuth2Context context) throws Exception {
		var formParams = UriComponentsBuilder.newInstance()
				.queryParam("client_id", clientId)
				.queryParam("client_secret", clientSecret)
				.queryParam("code", context.code)
				.queryParam("redirect_uri", context.currentUri)
				.queryParam("grant_type", "authorization_code");

		var request = HttpRequest
				.newBuilder(URI.create("https://www.googleapis.com/oauth2/v4/token"))
				.POST(BodyPublishers.ofString(formParams.build().getQuery()))
				.header("Content-Type", "application/x-www-form-urlencoded")
				.build();

		var res = httpClient.send(request, BodyHandlers.ofString());
		if (res.statusCode() != 200) {
			throw new Error("Oauth Error" + res.body());
		}
		var tokenEntity = objectMapper.readValue(res.body(), GoogleTokenResp.class);
		return getUserProfile(tokenEntity.access_token);
	}

	private UserInfo getUserProfile(String accessToken) throws IOException, InterruptedException {
		var uu = UriComponentsBuilder
				.fromUriString("https://www.googleapis.com/oauth2/v2/userinfo")
				.queryParam("fields", "id,name,picture")
				.build().toUri();

		var request = HttpRequest.newBuilder(uu)
				.header("Authorization", "Bearer " + accessToken)
				.build();

		var res = httpClient.send(request, BodyHandlers.ofString());
		return objectMapper.readValue(res.body(), GoogleUserInfo.class);
	}

	@AllArgsConstructor(onConstructor_ = @JsonCreator)
	private static final class GoogleTokenResp {
		public final String access_token;
		public final String refresh_token;
		public final String token_type;
		public final int expires_in; // 秒,默认1小时
	}

	@AllArgsConstructor(onConstructor_ = @JsonCreator)
	private static final class GoogleUserInfo implements UserInfo {

		/** 谷歌的ID特别长，不能用long */
		public final String id;
		public final String picture;
		public final String name;

		@Override
		public String id() {
			return id;
		}

		@Override
		public String head() {
			return picture;
		}

		@Override
		public String name() {
			return name;
		}
	}
}
