package net.kaciras.blog.facade.pojo;

import lombok.Data;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

@Data
public class CategoryVO {

	private int id;
	private String name;

	private ImageRefrence cover;
	private String description;
	private ImageRefrence background;

	private int level;
	private int parent;

	private int articleCount;
}
