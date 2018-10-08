package net.kaciras.blog.api.category;

import lombok.Data;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

@Data
public final class SimpleCategoryVo {

	private int id;
	private ImageRefrence cover;
	private String name;
}
