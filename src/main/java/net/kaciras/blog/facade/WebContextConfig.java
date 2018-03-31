package net.kaciras.blog.facade;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;
import net.kaciras.blog.infrastructure.codec.ImageRefrenceDeserializer;
import net.kaciras.blog.infrastructure.codec.ImageRefrenceSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Properties;

@Configuration
@ComponentScan("net.kaciras.blog.facade")
public class WebContextConfig {

	@Bean
	public static PlaceholderConfigurerSupport placeholderConfigurer(@Qualifier("config") Properties properties) {
		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		configurer.setProperties(properties);
		return configurer;
	}

	@Bean
	public ObjectMapper objectMapper() {
		SimpleModule module = new SimpleModule();
		module.addSerializer(ImageRefrence.class, new ImageRefrenceSerializer());
		module.addDeserializer(ImageRefrence.class, new ImageRefrenceDeserializer());

		ObjectMapper mapper = new ObjectMapper()
				.registerModule(module)
				.registerModule(new ParameterNamesModule())
				.registerModule(new Jdk8Module())
				.registerModule(new JavaTimeModule());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
	}
}
