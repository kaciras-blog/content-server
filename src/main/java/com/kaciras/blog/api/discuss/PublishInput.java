package com.kaciras.blog.api.discuss;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.kaciras.blog.infra.validate.NullOrNotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor(onConstructor_ = @JsonCreator)
@Getter
final class PublishInput {

	private final int objectId;
	private final int type;

	// 回复不需要 objectId 和 type，只需要被回复者的 ID 即可。
	private final int parent;

	@Length(max = 10)
	@NullOrNotBlank
	private final String nickname;

	// 数据库使用 TEXT 类型，一个字最多 4 字节，所以限制是 16383 字。
	@Length(max = 65536 / 4 - 1)
	@NotBlank
	private final String content;
}
