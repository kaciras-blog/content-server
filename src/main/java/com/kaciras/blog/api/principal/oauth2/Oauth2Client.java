package com.kaciras.blog.api.principal.oauth2;

import com.kaciras.blog.api.principal.AuthType;
import org.springframework.lang.Nullable;
import org.springframework.web.util.UriComponentsBuilder;

public interface Oauth2Client {

	AuthType authType();

	UriComponentsBuilder authUri();

	UserInfo getUserInfo(OAuth2Context context) throws Exception;

	interface UserInfo {

		/** 不能用整数，因为并非所有提供者都用整数作ID */
		String id();

		/** 第三方系统中的用户名 */
		String name();

		/** 第三方系统中用户的头像，可能不存在 */
		@Nullable
		String head();
	}
}
