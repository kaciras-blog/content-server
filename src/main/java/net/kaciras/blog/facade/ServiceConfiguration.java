package net.kaciras.blog.facade;

import net.kaciras.blog.domain.CommonConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Import(CommonConfiguration.class)
@Configuration
public class ServiceConfiguration {

	@Bean
	public Properties config() {
		Properties properties = new Properties();
		loadIgnoreException(properties, "config.ini");
		loadIgnoreException(properties, "../config.ini");
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
