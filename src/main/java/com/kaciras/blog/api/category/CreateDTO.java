package com.kaciras.blog.api.category;

import com.kaciras.blog.infra.codec.ImageReference;
import lombok.RequiredArgsConstructor;

/**
 * 包含一个分类自身相关的属性，不包括id，parent 等用于关联的字段，或是其他一些统计数据。
 */
@RequiredArgsConstructor
final class CreateDTO {

	public final String name;
	public final ImageReference cover;
	public final String description;
	public final ImageReference background;
	public final int theme;
}
