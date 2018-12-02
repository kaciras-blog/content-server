package net.kaciras.blog.api.misc;

import lombok.Data;
import lombok.ToString;

@ToString(of = {"name", "link"})
@Data
final class SwiperSlide {

	private String name;
	private String picture;
	private String link;
	private String description;
}
