package net.kaciras.blog.api.principal.local;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@AllArgsConstructor(onConstructor_ = @JsonCreator)
@Getter
final class LoginRequest {

	@Length(min = 1, max = 16)
	private final String name;

	@Length(min = 8, max = 128)
	private final String password;

	private final boolean remember;
}
