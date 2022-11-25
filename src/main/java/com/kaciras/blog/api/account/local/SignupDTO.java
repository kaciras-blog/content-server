package com.kaciras.blog.api.account.local;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.Length;

@AllArgsConstructor
final class SignupDTO {

	@Pattern(regexp = "^[\\u4E00-\\u9FFFa-zA-Z0-9_]{1,16}$")
	@NotNull
	public final String name;

	// 128 位可见 ASCII 已经超出了 HASH 的长度，更长也没意义
	@Length(min = 8, max = 128)
	@NotNull
	public final String password;

	@NotEmpty
	public final String captcha;
}
