package net.kaciras.blog.api.misc;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.kaciras.blog.infra.codec.ImageReference;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ToString(of = {"name", "link"})
@Getter
@Setter
final class SlideCard {

	@Length(max = 20)
	private String name;

	@NotEmpty
	private String link;

	@NotNull
	private ImageReference picture;

	@Length(max = 100)
	private String description;
}
