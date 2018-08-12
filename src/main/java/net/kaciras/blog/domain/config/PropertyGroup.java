package net.kaciras.blog.domain.config;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
final class PropertyGroup {

	private final String desc;

	private Map<String, PropertyItem> items = new HashMap<>();
}
