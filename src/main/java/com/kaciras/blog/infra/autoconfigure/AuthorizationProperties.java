package com.kaciras.blog.infra.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;

@ConfigurationProperties("app.authorization")
@Getter
@Setter
public final class AuthorizationProperties {

	/** 所有用户的身份都设为管理员，在调试时有用. */
	private boolean adminPrincipal;

	private CSRFProperties csrf = new CSRFProperties();

	@Getter
	@Setter
	public static final class CSRFProperties {

		private boolean dynamicCookie;

		/** 在Cookie中存储CSRF Token，如果为null则不启用CSRF检查. */
		private String cookieName = "CSRF-Token";

		/** 在处理HTTP安全请求时不做CSRF相关的检查. */
		private boolean skipSafe = true;

		/** 鉴定用户身份时要求检查该请求头的值与CSRF Cookie的值相等. */
		@Nullable
		private String headerName = "X-CSRF-Token";

		/** 鉴定用户身份时要求检查该请求参数的值与CSRF Cookie的值相等. */
		@Nullable
		private String parameterName;
	}
}
