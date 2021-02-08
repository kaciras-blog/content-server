package com.kaciras.blog.api.category;

import com.kaciras.blog.infra.codec.ImageReference;

/**
 * 包含一个分类自身相关的属性，不包括id，parent 等用于关联的字段，或是其他一些统计数据。
 */
final class CreateDTO {

	public String name;
	public ImageReference cover;
	public String description;
	public ImageReference background;
	public int theme;
}
