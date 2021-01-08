package com.kaciras.blog.api.account.oauth2;

import com.kaciras.blog.api.account.AuthType;
import org.springframework.web.util.UriComponentsBuilder;

public interface OAuth2Client {

	AuthType authType();

	/**
	 * OAuth2 第一步，获取第三方系统 OAuth 授权页面的 URL 模板。
	 */
	UriComponentsBuilder authUri();

	/**
	 * OAuth2 最后一步，使用用户传递的授权码去第三方系统获取数据。
	 *
	 * @param context 一些必要的信息
	 * @return 第三方系统中的用户数据
	 */
	UserProfile getUserInfo(OAuth2Context context) throws Exception;
}
