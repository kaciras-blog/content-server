package net.kaciras.blog.api.category;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

@ToString(of = {"id", "name"})
@Getter
@Setter
public final class SimpleCategoryVo {

	private int id;
	private ImageRefrence cover;
	private String name;
}
