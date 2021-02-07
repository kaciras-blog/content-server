package com.kaciras.blog.api.account.oauth2;

import com.kaciras.blog.api.account.AuthType;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 对各种第三方系统适配的接口，是 OAuth2 中用于本系统和第三方认证提供者通信的类。
 *
 * <h2>不支持配置式</h2>
 * 一种想法是把各种系统不同的地方都作为配置项，Spring 就是这样做的：
 * {@code org.springframework.security.config.oauth2.client.CommonOAuth2Provider}
 *
 * 这种做法多了个配置层，对于本项目而言没有必要，所以这里直接用代码来做抽象了。
 */
public interface OAuth2Client {

	AuthType authType();

	/**
	 * OAuth2 第一步，获取第三方系统授权页面的 URL 模板。
	 */
	UriComponentsBuilder uriTemplate();

	/**
	 * OAuth2 最后一步，使用用户传递的授权码去第三方系统获取数据。
	 *
	 * @param context 一些必要的信息
	 * @return 第三方系统中的用户数据
	 */
	UserProfile getUserInfo(OAuth2Context context) throws Exception;
}
