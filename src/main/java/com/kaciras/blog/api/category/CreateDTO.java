package com.kaciras.blog.api.category;

import com.kaciras.blog.infra.codec.ImageReference;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;

/**
 * 包含一个分类自身相关的属性，不包括 id，parent 等用于关联的字段，或是其他一些统计数据。
 */
@AllArgsConstructor
final class CreateDTO {

	@NotBlank
	public final String name;

	public final ImageReference cover;

	@NotEmpty
	public final String description;

	public final int theme;

	public final ImageReference background;
}
