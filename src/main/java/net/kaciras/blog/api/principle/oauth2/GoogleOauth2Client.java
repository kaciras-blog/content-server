package net.kaciras.blog.api.principle.oauth2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.principle.AuthType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class GoogleOauth2Client implements Oauth2Client {

	private static final String CLIENT_ID = "834515199449-f1ia0v8b3fa5hnvuimrradoetulc5nvi.apps.googleusercontent.com";
	private static final String CLIENT_SECRET = "jT7m_0N5zS-QzuzwGKYnYuFo";

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;

	@Override
	public AuthType authType() {
		return AuthType.Google;
	}

	@Override
	public UriComponentsBuilder authUri() {
		return UriComponentsBuilder
				.fromUriString("https://accounts.google.com/o/oauth2/auth")
				.queryParam("client_id", CLIENT_ID)
				.queryParam("scope", "https://www.googleapis.com/auth/userinfo.profile")
				.queryParam("state", UUID.randomUUID().toString())
				.queryParam("response_type", "code")
				.queryParam("access_type", "offline");
	}

	@Override
	public UserInfo getUserInfo(String code, @Nullable String state) throws Exception {
		var formParams = UriComponentsBuilder.newInstance()
				.queryParam("client_id", CLIENT_ID)
				.queryParam("client_secret", CLIENT_SECRET)
				.queryParam("code", code)
				.queryParam("redirect_uri", "https://localhost:2375/connect/google/callback")
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

		var res = httpClient.send(request, BodyHandlers.ofInputStream());
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

		public final long id;
		public final String picture;
		public final String name;

		@Override
		public long id() {
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
