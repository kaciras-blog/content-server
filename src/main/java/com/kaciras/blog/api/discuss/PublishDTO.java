package com.kaciras.blog.api.discuss;

import com.kaciras.blog.infra.validate.NullOrNotBlank;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
final class PublishDTO {

	public final int objectId;
	public final int type;

	// 回复不需要 objectId 和 type，只需要被回复者的 ID 即可。
	public final int parent;

	@Length(max = 10)
	@NullOrNotBlank
	public final String nickname;

	// 数据库使用 TEXT 类型，一个字最多 4 字节，所以限制是 16383 字。
	@Length(max = 65536 / 4 - 1)
	@NotBlank
	public final String content;
}
