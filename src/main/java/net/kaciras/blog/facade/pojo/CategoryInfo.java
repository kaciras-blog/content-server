package net.kaciras.blog.facade.pojo;

import lombok.Data;

@Data
public class CategoryInfo {

	private int id;
	private String name;

	private String cover;
	private String description;
	private String background;

	private int level;

	//TODO: 考虑冗余文章数在分类表中
	private int articleCount;
}
