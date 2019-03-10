package net.kaciras.blog.api.principle.local;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
@Getter
final class RegisterRequest {

	@Pattern(regexp = "^[\\u4E00-\\u9FFFa-zA-Z0-9_]{1,16}$")
	private final String name;

	@Length(min = 8, max = 128)
	private final String password;

	@NotEmpty
	private final String captcha;
}
