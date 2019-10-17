package net.kaciras.blog.api.misc;

import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.infra.codec.ImageReference;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public final class FriendLink {

	@Length(min = 1, max = 16)
	private String name;

	@NotEmpty
	private String url;

	@NotNull
	private ImageReference favicon;
}
