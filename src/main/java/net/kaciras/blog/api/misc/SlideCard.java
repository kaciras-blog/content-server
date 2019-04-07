package net.kaciras.blog.api.misc;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.kaciras.blog.infrastructure.codec.ImageReference;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@ToString(of = {"name", "link"})
@Getter
@Setter
final class SlideCard {

	@Length(max = 20)
	private String name;

	@NotEmpty
	private String link;

	@NotEmpty
	private ImageReference picture;

	@Length(max = 100)
	private String description;
}
