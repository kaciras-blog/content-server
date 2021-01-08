package com.kaciras.blog.api.account.oauth2;

import org.springframework.lang.Nullable;

public interface UserProfile {

	/**
	 * 第三方系统中账户的 ID。
	 * 注意 OAuth 并未限制 ID 是整数，所以用字符串。
	 */
	String id();

	/**
	 * 第三方系统中的用户名。
	 */
	String name();

	/**
	 * 第三方系统中用户的头像，可能不存在。
	 */
	@Nullable
	default String avatar() { return null; }
}
