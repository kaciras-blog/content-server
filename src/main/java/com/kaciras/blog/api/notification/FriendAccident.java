package com.kaciras.blog.api.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.time.Instant;

/**
 * 用 Accident 是不是不太好……
 */
@RequiredArgsConstructor
@Getter
public final class FriendAccident {

	public enum Type {
		AbandonedMe,
		Inaccessible,
	}

	private final Type type;
	private final String name;
	private final URI url;
	private final Instant time;
}
