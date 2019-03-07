package net.kaciras.blog.api.principle.local;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.hibernate.validator.constraints.Length;

@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
@Value
final class LoginRequest {

	@Length(min = 1, max = 16)
	private String name;

	@Length(min = 8, max = 128)
	private String password;

	private boolean remember;
}
