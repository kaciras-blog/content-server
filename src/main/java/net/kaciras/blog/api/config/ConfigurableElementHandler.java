package net.kaciras.blog.api.config;

import lombok.Getter;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;

import java.util.HashMap;
import java.util.Map;

final class ConfigurableElementHandler implements ElementHandler {

	@Getter
	private Map<String, PropertyGroup> configurables = new HashMap<>();

	private PropertyGroup currentGroup;

	@Override
	public void onStart(ElementPath elementPath) {
		var element = elementPath.getCurrent();
		if (element.getName().equals("group")) {
			currentGroup = new PropertyGroup(element.attributeValue("desc"));
		}
	}

	@Override
	public void onEnd(ElementPath elementPath) {
		var element = elementPath.getCurrent();
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
		var type = element.attributeValue("type");
		var name = element.attributeValue("key");

		switch (type) {
			case "int":
			case "string":
			case "bool":
			case "float":
				currentGroup.getItems().put(name, new PropertyItem(type, element.attributeValue("desc")));
				break;
			default:
				currentGroup.getItems().put(name, new EnumPropertyItem(type, element.attributeValue("desc")));
		}
	}
}
