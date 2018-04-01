package net.kaciras.blog.domain.config;

import net.kaciras.blog.infrastructure.message.MessageClient;
import net.kaciras.blog.infrastructure.message.event.ConfigChangedEvent;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

@Service
public class ConfigService {

	private final Properties properties;
	private final MessageClient messageClient;

	private Map<String, PropertyGroup> configurables;

	public ConfigService(@Qualifier("config") Properties properties,
						 MessageClient messageClient) {
		this.properties = properties;
		this.messageClient = messageClient;
	}

	@PostConstruct
	private void loadModifiable() throws IOException, DocumentException {
		InputStream stream = ConfigService.class.getClassLoader().getResourceAsStream("configurable.xml");
		try (stream) {
			ConfigurableElementHandler handler = new ConfigurableElementHandler();
			SAXReader saxReader = new SAXReader();
			saxReader.setEncoding("UTF-8");
			saxReader.setDefaultHandler(handler);
			saxReader.read(stream);
			configurables = handler.getConfigurables();
		}
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public void set(String key, Object value) {
		properties.put(key, value);
		ConfigChangedEvent event = new ConfigChangedEvent();
		event.setKey(key);
		event.setOldValue(properties.getProperty(key));
		event.setNewValue(value.toString());
		messageClient.send(event);
	}

	public Map<String, PropertyGroup> getModifiable() {
		return configurables;
	}
}
