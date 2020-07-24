package com.kaciras.blog.api.friend;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;

import java.time.Instant;

@AllArgsConstructor(onConstructor_ = @JsonCreator)
final class ValidateRecord {

	public String url;

	@Nullable
	public String friendPage;

	/** 验证时间 */
	public Instant validate;

	/** 验证失败次数 */
	public int failed;
}
