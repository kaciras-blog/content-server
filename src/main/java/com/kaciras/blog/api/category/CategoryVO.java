package com.kaciras.blog.api.category;

import com.kaciras.blog.infra.codec.ImageReference;

public class CategoryVO {

	public int id;
	public int parent;

	public String name;
	public ImageReference cover;
	public String description;
	public ImageReference background;
	public int theme;
	
	public int articleCount;

	public Banner banner;
}
