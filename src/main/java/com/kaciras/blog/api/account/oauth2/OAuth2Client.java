package com.kaciras.blog.api.account.oauth2;

import com.kaciras.blog.api.account.AuthType;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 对各种第三方系统适配的接口，是 OAuth2 中用于本系统和第三方认证提供者通信的类。
 *
 * <h2>纯配置不可行</h2>
 * 一种想法是把各种系统不同的地方都作为配置项，但获取数据的请求没有统一的规范，
 * 各个系统相差很大，比如 GitHub 是 JSON 响应而 Google 不是而且还要设置 fields 参数。
 * 这些不同的地方无法用配置解决，只能写在代码里。
 */
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
