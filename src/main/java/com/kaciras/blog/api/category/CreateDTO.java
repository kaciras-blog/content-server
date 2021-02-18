package com.kaciras.blog.api.category;

import com.kaciras.blog.infra.codec.ImageReference;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 包含一个分类自身相关的属性，不包括id，parent 等用于关联的字段，或是其他一些统计数据。
 */
@AllArgsConstructor
final class CreateDTO {

	@NotBlank
	public final String name;

	@NotNull
	public final ImageReference cover;

	@NotEmpty
	public final String description;

	public final int theme;

	@NotNull
	public final ImageReference background;
}
