package net.kaciras.blog.category;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryVo extends CategoryAttributes {

	private int id;

	private int level;
	private int parent;
	private int articleCount;
}
