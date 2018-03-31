package net.kaciras.blog.domain.config;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(of = "key")
@Data
class PropertyItem {

	private final String type;
	private final String key;
	private final String desc;
}
