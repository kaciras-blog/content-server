package net.kaciras.blog.api.principal.oauth2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 打包里获取AccessToken时，各种不同的提供者可能需要的信息。
 * Github可以再次发送state、google需要再传一次callback的URI，这里统一提供。
 */
@RequiredArgsConstructor
@Getter
public final class OAuth2Context {

	private final String code;
	private final String currentUri;
	private final String state;
}
