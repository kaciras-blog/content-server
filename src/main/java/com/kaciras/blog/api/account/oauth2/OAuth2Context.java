package com.kaciras.blog.api.account.oauth2;

import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * OAuth2 验证码模式中，存储重定向和回调之间信息的对象。
 */
@AllArgsConstructor
final class OAuth2Context implements Serializable {

	/**
	 * 第三方的名字。
	 */
	public final String provider;

	/**
	 * 请求中的状态字段。
	 */
	public final String state;

	/**
	 * 登录完成后返回的地址。
	 */
	public final String returnUri;

	/**
	 * 登录开始的时间。
	 */
	public final Instant time;
}
