package net.kaciras.blog.facade.pojo;

import lombok.Data;

@Data
public class CategoryVO {

	private int id;
	private String name;

	private String cover;
	private String description;
	private String background;

	private int level;
	private int parent;

	private int articleCount;
}
