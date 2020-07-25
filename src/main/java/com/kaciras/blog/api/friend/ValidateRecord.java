package com.kaciras.blog.api.friend;

import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.time.Instant;

@NoArgsConstructor
final class ValidateRecord {

	public String url;

	@Nullable
	public String friendPage;

	/** 验证时间 */
	public Instant validate;

	/** 验证失败次数 */
	public int failed;

	public ValidateRecord(String url, @Nullable String friendPage, Instant validate) {
		this.url = url;
		this.friendPage = friendPage;
		this.validate = validate;
	}
}
