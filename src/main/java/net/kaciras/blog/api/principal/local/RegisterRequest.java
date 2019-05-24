package net.kaciras.blog.api.principal.local;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@AllArgsConstructor(onConstructor_ = @JsonCreator)
@Getter
final class RegisterRequest {

	@Pattern(regexp = "^[\\u4E00-\\u9FFFa-zA-Z0-9_]{1,16}$")
	private final String name;

	// 128 位以上的密码超出了哈希输出的位数，没意义
	@Length(min = 8, max = 128)
	private final String password;

	@NotEmpty
	private final String captcha;
}
