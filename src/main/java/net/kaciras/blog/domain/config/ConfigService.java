package net.kaciras.blog.domain.config;

import net.kaciras.blog.infrastructure.event.ConfigChangedEvent;
import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import net.kaciras.blog.infrastructure.message.MessageClient;
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

	private final MessageClient messageClient;
	private final Properties properties;
	private final ConvertorRegistery convertorRegistery;

	private Map<String, PropertyGroup> configurables;

	public ConfigService(@Qualifier("config") Properties properties,
						 MessageClient messageClient,
						 ConvertorRegistery convertorRegistery) {
		this.properties = properties;
		this.messageClient = messageClient;
		this.convertorRegistery = convertorRegistery;
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

	public void set(String key, String value) {
		/* 检查配置项的值是否符合定义的类型 */
		String[] tupe = key.split("\\.", 2);
		PropertyItem item = configurables.get(tupe[0]).getItems().get(tupe[1]);
		if(!convertorRegistery.checkType(item.getType(), value)) {
			throw new RequestArgumentException("配置项的值不符合定义的类型");
		}

		properties.put(key, value);
		messageClient.send(new ConfigChangedEvent(key, properties.getProperty(key), value));
	}

	public Map<String, PropertyGroup> getModifiable() {
		return configurables;
	}
}
