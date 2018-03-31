package net.kaciras.blog.domain.config;

import lombok.Getter;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;

import java.util.HashMap;
import java.util.Map;

public class ConfigurableElementHandler implements ElementHandler {

	@Getter
	private Map<String, PropertyGroup> configurables = new HashMap<>();

	private PropertyGroup currentGroup;

	@Override
	public void onStart(ElementPath elementPath) {
		Element element = elementPath.getCurrent();
		if (element.getName().equals("group")) {
			currentGroup = new PropertyGroup(element.attributeValue("name"), element.attributeValue("desc"));
		}
	}

	@Override
	public void onEnd(ElementPath elementPath) {
		Element element = elementPath.getCurrent();
		switch (element.getName()) {
			case "group":
				configurables.put(element.attributeValue("name"), currentGroup);
				break;
			case "property":
				parseProperty(element);
				break;
		}
	}

	private void parseProperty(Element element) {
		String type = element.attributeValue("type");
		String name = element.attributeValue("key");

		switch (type) {
			case "int":
			case "string":
			case "bool":
			case "float":
				currentGroup.getItems().put(name, new PropertyItem(type, name, element.attributeValue("desc")));
				break;
			case "enum":
				currentGroup.getItems().put(name, new EnumPropertyItem(
						element.attributeValue("class"), name, element.attributeValue("desc")));
				break;
			default:
				throw new RuntimeException("未知的配置项类型" + type);
		}
	}
}
