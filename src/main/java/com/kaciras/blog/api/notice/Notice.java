package com.kaciras.blog.api.notice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

/**
 * 通知对象，表示一条通知信息。
 */
@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
@Getter
final class Notice {

	/**
	 * 通知的类型，虽然是从 {@code Activity} 的方法直接获取的，
	 * 但因为通知模块不知道消息的类型，所以这个值必须拿到外层。
	 */
	private final ActivityType type;

	/**
	 * 通知创建的时间。
	 */
	private final Instant time;

	/**
	 * 具体的内容，是一个 JSON 值。
	 *
	 * <h2>动态类型的选择</h2>
	 * 因为通知模块不知道消息的类型，所以这里只能使用通用的类型。
	 * bilibili 的方案是把它序列化为字符串，然后作为外层的一个属性又序列化一次。
	 * 我觉得这样很别扭，所以选择了通用的 JsonNode 类型避免重复序列化。
	 *
	 * 另一种方法是 @JsonRawValue 但是它不支持反序列化暂时还没法用。
	 */
	private final JsonNode data;
}
