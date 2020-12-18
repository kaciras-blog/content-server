package com.kaciras.blog.api.discuss;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.kaciras.blog.api.NullOrNotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor(onConstructor_ = @JsonCreator)
@Getter
final class PublishInput {

	private final int objectId;
	private final int type;

	private final int parent;

	@Length(max = 16)
	@NullOrNotBlank
	private final String nickname;

	// 底层使用 TEXT 类型，utf8mb4 一个字最多4字节
	@Length(max = 65536 / 4 - 1)
	@NotBlank
	private final String content;
}
