package net.kaciras.blog.api.misc;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@ToString(of = {"name", "link"})
@Getter
@Setter
final class SwiperSlide {

	@NotEmpty
	private String name;

	@NotEmpty
	private String link;

	@NotEmpty
	private String picture;

	@Length(max = 255)
	private String description;
}
