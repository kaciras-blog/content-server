package net.kaciras.blog.api.principle.local;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;

@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
final class LoginRequest {

	@Length(min = 1, max = 16)
	public final String name;

	@Length(min = 8, max = 128)
	public final String password;

	public final boolean remember;
}
