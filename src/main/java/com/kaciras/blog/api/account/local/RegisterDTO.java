package com.kaciras.blog.api.account.local;

import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@AllArgsConstructor
final class RegisterDTO {

	@Pattern(regexp = "^[\\u4E00-\\u9FFFa-zA-Z0-9_]{1,16}$")
	public final String name;

	// 128 位以上的密码超出了哈希输出的位数，没意义
	@Length(min = 8, max = 128)
	public final String password;

	@NotEmpty
	public final String captcha;
}
