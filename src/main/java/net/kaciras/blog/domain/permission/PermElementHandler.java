package net.kaciras.blog.domain.permission;

import lombok.Getter;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;

import java.util.HashMap;
import java.util.Map;

public class PermElementHandler implements ElementHandler {

	@Getter
	private final Map<String, Map<String, Permission>> map = new HashMap<>();

	private String currentModule;
	private Map<String, Permission> perms;

	@Override
	public void onStart(ElementPath elementPath) {
		Element element = elementPath.getCurrent();
		if (element.getName().equals("permissions")) {
			perms = new HashMap<>();
			currentModule = element.attributeValue("module");
		}
	}

	@Override
	public void onEnd(ElementPath elementPath) {
		Element element = elementPath.getCurrent();
		switch (element.getName()) {
			case "permissions":
				map.put(currentModule, perms);
				break;
			case "permission":
				String name = element.elementText("name");
				perms.put(name, new Permission(name, element.elementText("desc")));
				break;
		}
	}
}
