package com.kaciras.blog.infra.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.development")
@Getter
@Setter
public final class DevelopmentProperties {

	/** 增加请求处理的延迟，可用于测试. */
	private Duration httpDelay;

	private boolean debugErrorMessage;
}
