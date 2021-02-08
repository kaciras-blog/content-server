package com.kaciras.blog.api.article;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

/**
 * 定义Article的API中PATCH方法的参数。
 */
@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
final class PatchDTO {

	public final Integer category;
	public final Boolean deletion;
	public final String urlTitle;
}
