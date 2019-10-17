package net.kaciras.blog.api.category;

import lombok.Value;
import net.kaciras.blog.infra.codec.ImageReference;

@Value
public final class Banner {

	private ImageReference image;
	private int theme;
}
