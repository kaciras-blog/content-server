package net.kaciras.blog.api.principle.oauth2;

import net.kaciras.blog.api.principle.AuthType;
import org.springframework.lang.Nullable;
import org.springframework.web.util.UriComponentsBuilder;

public interface Oauth2Client {

	AuthType authType();

	UriComponentsBuilder authUri();

	UserInfo getUserInfo(String code, @Nullable String state) throws Exception;

	interface UserInfo {

		long id();

		String name();

		@Nullable
		String head();
	}
}
