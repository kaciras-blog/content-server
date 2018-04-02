package net.kaciras.blog.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Configuration
@Profile("test")
@Import(CommonConfiguration.class)
public class TestContextConfig {

	@Bean
	public Properties config() throws IOException {
		Properties properties = new Properties();
		String confile = "D:\\Coding\\JAVA\\Blog-V7\\Service\\src\\test\\resources\\config.ini";
		try (Reader reader = new InputStreamReader(new FileInputStream(confile), StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		return properties;
	}
}
