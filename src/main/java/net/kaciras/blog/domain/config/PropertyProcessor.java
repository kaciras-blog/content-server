package net.kaciras.blog.domain.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

class PropertyProcessor implements EnvironmentPostProcessor {

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		Path file = Paths.get(getDataDirectory(environment), "config.properties");
		Properties properties = new Properties();

		try{
			InputStream input = new FileInputStream(file.toFile());
			try (input) {
				properties.load(input);
			}
		} catch (FileNotFoundException ignore) {
			//ignored,and an empty Properties will be registried.
		} catch (IOException ex) {
			throw new ApplicationContextException("加载配置失败", ex);
		}

		environment.getPropertySources().addFirst(new PropertiesPropertySource("ConfigServiceSource", properties));
	}

	private String getDataDirectory(Environment environment) {
		try {
			if (environment.getProperty("temporary-data", Boolean.class, false)) {
				return Files.createTempDirectory("blog-data-").toString();
			}
			String dataDir = environment.getProperty("data-directory");
			if (dataDir == null) {
				throw new ApplicationContextException("请在配置文件中指定data-directory或将temporary-data设为true");
			}
			return dataDir;
		} catch (IOException e) {
			throw new ApplicationContextException("无法创建数据目录", e);
		}
	}
}
