package com.kaciras.blog.infra.autoconfigure;

import com.kaciras.blog.infra.codec.ExtendsCodecModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注册与 codec 包下的类相关的序列化处理器。
 */
@ConditionalOnClass(Jackson2ObjectMapperBuilderCustomizer.class)
@Configuration(proxyBeanMethods = false)
public class BlogJsonAutoConfiguration {

	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jacksonCodecCustomizer() {
		return builder -> builder.modulesToInstall(ExtendsCodecModule.class);
	}
}
