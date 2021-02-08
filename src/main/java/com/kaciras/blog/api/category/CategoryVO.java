package com.kaciras.blog.api.category;

import com.kaciras.blog.infra.codec.ImageReference;

import java.util.List;

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

	/**
	 * 聚合视图，除了分类本身的属性之外还包含了子分类。
	 */
	public List<CategoryVO> children;
}
