package net.kaciras.blog.api.principle.local;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
@Value
final class RegisterRequest {

	@Pattern(regexp = "^[\\u4E00-\\u9FFFa-zA-Z0-9_]{1,16}$")
	private String name;

	@Length(min = 8, max = 128)
	private String password;

	@NotEmpty
	private String captcha;
}
