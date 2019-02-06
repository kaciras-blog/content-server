package net.kaciras.blog.api.misc;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(of = {"name", "link"})
@Getter
@Setter
final class SwiperSlide {

	private String name;
	private String picture;
	private String link;
	private String description;
}
