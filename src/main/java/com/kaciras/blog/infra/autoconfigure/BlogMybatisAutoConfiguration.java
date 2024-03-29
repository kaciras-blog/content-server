package com.kaciras.blog.infra.autoconfigure;

import com.kaciras.blog.infra.MybatisMapperAspect;
import com.kaciras.blog.infra.codec.ImageReferenceTypeHandler;
import com.kaciras.blog.infra.codec.InetAddressTypeHandler;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@ConditionalOnClass(ConfigurationCustomizer.class)
@AutoConfiguration
public class BlogMybatisAutoConfiguration {

	@Bean
	public MybatisMapperAspect mapperAspect() {
		return new MybatisMapperAspect();
	}

	@Bean
	public ConfigurationCustomizer mybatisCustomizer() {
		return config -> {
			var registry = config.getTypeHandlerRegistry();
			registry.register(ImageReferenceTypeHandler.class);
			registry.register(InetAddressTypeHandler.class);
		};
	}
}
