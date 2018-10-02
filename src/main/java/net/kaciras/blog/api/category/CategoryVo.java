package net.kaciras.blog.api.category;

import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

import java.util.List;

@Getter
@Setter
public class CategoryVo extends CategoryAttributes {

	private int id;

	private int parent;
	private int level;

	private int articleCount;
	private ImageRefrence bestBackground;
	private List<CategoryVo> children;
}
