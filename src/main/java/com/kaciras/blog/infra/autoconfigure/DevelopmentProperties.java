package com.kaciras.blog.infra.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.development")
@Getter
@Setter
public final class DevelopmentProperties {

	private boolean debugErrorMessage;
}
