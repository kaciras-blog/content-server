package com.kaciras.blog.api.account.oauth2;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 打包获取 AccessToken 时，各种不同的提供者可能需要的信息。
 * Github 可以再次发送 state、google 需要再传一次 callback 的 URI。
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public final class AuthorizeRequest {

	private final String code;

	private final String state;

	private final String redirectUri;
}
