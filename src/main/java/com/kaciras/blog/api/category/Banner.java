package com.kaciras.blog.api.category;

import com.kaciras.blog.infra.codec.ImageReference;
import lombok.Value;

@Value
public final class Banner {

	private ImageReference image;
	private int theme;
}
