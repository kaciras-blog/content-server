package net.kaciras.blog.api.misc;

import lombok.Data;

import java.io.Serializable;

@Data
final class SwiperSlide implements Serializable {

	private String name;
	private String picture;
	private String link;
	private String description;
}
