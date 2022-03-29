package com.kaciras.blog.api.draft;

import com.kaciras.blog.infra.codec.ImageReference;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class DraftContent {

	@NotEmpty
	private String title;

	/** 封面可以没有 */
	private ImageReference cover;

	// 下面三个不能为 null，但可以为空串。

	@NotNull
	private String keywords;

	@NotNull
	private String summary;

	@NotNull
	private String content;
}
