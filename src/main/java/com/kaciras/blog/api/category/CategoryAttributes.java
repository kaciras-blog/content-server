package com.kaciras.blog.api.category;

import com.kaciras.blog.infra.codec.ImageReference;
import lombok.Getter;
import lombok.Setter;

/**
 * 包含一个分类自身相关的属性，不包括id，parent等用于
 * 关联的字段，或是其他一些统计数据。
 */
@Getter
@Setter
public class CategoryAttributes {

	private String name;
	private ImageReference cover;
	private String description;
	private ImageReference background;
	private int theme;
}
