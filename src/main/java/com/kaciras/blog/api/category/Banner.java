package com.kaciras.blog.api.category;

import com.kaciras.blog.infra.codec.ImageReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class Banner {

	private final ImageReference image;

	private final int theme;
}
