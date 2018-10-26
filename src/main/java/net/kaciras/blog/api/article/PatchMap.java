package net.kaciras.blog.api.article;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * 定义Article的API中PATCH方法的参数。
 */
@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
@Value
final class PatchMap {

	private final Integer category;
	private final Boolean deletion;
}
