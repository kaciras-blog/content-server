package com.kaciras.blog.api.friend;

import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;

import java.net.URI;
import java.time.Instant;

@AllArgsConstructor
final class ValidateRecord {

	public URI url;

	@Nullable
	public URI friendPage;

	/** 验证时间 */
	public Instant validate;

	/** 验证失败次数 */
	public int failed;
}
