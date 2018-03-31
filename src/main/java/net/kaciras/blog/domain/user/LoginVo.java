package net.kaciras.blog.domain.user;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public final class LoginVo {

	@Length(min = 1, max = 16)
	private String name;

	@Length(min = 8, max = 128)
	private String password;

	private boolean remenber;
}
