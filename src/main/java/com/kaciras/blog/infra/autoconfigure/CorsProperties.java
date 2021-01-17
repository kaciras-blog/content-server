package com.kaciras.blog.infra.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.web.cors.CorsConfiguration;

import java.time.Duration;
import java.util.List;

// DefaultValue 不能放到自动上，无法搭配 RequiredArgsConstructor，所以暂不使用 ConstructorBinding
@ConfigurationProperties("app.cors")
@Getter
@Setter
public final class CorsProperties {

	@Nullable
	private CorsTemplate template;

	private List<String> allowedOrigins;
	private List<String> allowedOriginPatterns;

	private Boolean allowCredentials;

	private List<String> allowedMethods;
	private List<String> allowedHeaders;
	private List<String> exposedHeaders;

	/**
	 * 设置缓存时间，浏览器通常有自己的限制（Firefox:86400，Chrome/Blink: 600）。
	 */
	private Duration maxAge = Duration.ofDays(1);

	public enum CorsTemplate {

		/**
		 * 将CORS配置为Spring中的默认状态。
		 *
		 * @see CorsConfiguration#applyPermitDefaultValues()
		 */
		Default,

		/**
		 * 将CORS配置为允许所有（Origin，Allowed-Headers，Method...），这些属性都设为"*"
		 * 注意 Exposed-Headers 不支持通配而必须手动设置
		 */
		AllowAll
	}
}
