package net.kaciras.blog.api.category;

import lombok.Value;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

@Value
public final class Banner {

	private ImageRefrence image;
	private int theme;
}
