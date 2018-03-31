package net.kaciras.blog.domain.config;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(of = "name")
@Data
final class PropertyGroup {

	private final String name;
	private final String desc;

	private Map<String, PropertyItem> items = new HashMap<>();
}
