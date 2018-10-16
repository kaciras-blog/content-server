package net.kaciras.blog.api.category;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryVo extends CategoryAttributes {

	private int id;
	private int level;

	private int articleCount;
}
