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
public class TestContextConfig {

	@Bean
	public Properties config() {
		Properties properties = new Properties();
		loadIgnoreException(properties, "D:\\Coding\\JAVA\\Blog\\Service\\src\\test\\resources\\config.ini");
		return properties;
	}

	private void loadIgnoreException(Properties properties, String file) {
		try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
			properties.load(reader);
		} catch (IOException ignore) {
			// ignore
		}
	}

}
