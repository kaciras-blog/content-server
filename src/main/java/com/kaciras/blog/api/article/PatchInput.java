package com.kaciras.blog.api.article;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 定义Article的API中PATCH方法的参数。
 */
@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
@Getter
final class PatchInput {

	private final Integer category;
	private final Boolean deletion;
	private final String urlTitle;
}
