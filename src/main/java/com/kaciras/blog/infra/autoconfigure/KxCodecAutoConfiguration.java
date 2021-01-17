package com.kaciras.blog.infra.autoconfigure;

import com.kaciras.blog.infra.codec.ExtendsCodecModule;
import com.kaciras.blog.infra.codec.ImageReferenceTypeHandler;
import com.kaciras.blog.infra.codec.InetAddressTypeHandler;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动注册与 com.kaciras.blog.infrastructure.codec 包下的类相关的基础设施，
 * 包括Jackson的序列号模块、mybatis的TypeHandler。
 */
@Configuration(proxyBeanMethods = false)
public class KxCodecAutoConfiguration {

	@ConditionalOnClass(ConfigurationCustomizer.class)
	@Configuration(proxyBeanMethods = false)
	static class MybatisConfiguration {

		@Bean
		public ConfigurationCustomizer mybatisCustomizer() {
			return config -> {
				var registry = config.getTypeHandlerRegistry();
				registry.register(ImageReferenceTypeHandler.class);
				registry.register(InetAddressTypeHandler.class);
			};
		}
	}

	@ConditionalOnClass(Jackson2ObjectMapperBuilderCustomizer.class)
	@Configuration(proxyBeanMethods = false)
	static class JacksonConfiguration {

		@SuppressWarnings("unchecked")
		@Bean
		public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
			return builder -> builder.modulesToInstall(ExtendsCodecModule.class);
		}
	}
}
