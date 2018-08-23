package net.kaciras.blog.recommend;

import lombok.Data;

import java.io.Serializable;

@Data
public class SwipPage implements Serializable {

	private String picture;
	private String link;
	private String description;
}
