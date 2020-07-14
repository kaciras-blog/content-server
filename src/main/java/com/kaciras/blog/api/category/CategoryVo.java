package com.kaciras.blog.api.category;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryVo extends CategoryAttributes {

	private int id;
	private int parent;

	private int articleCount;

	private Banner banner;
}
