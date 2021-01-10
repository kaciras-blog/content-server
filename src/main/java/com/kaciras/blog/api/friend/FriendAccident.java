package com.kaciras.blog.api.friend;

import com.kaciras.blog.api.notice.HttpNotice;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.time.Instant;

/**
 * 用 Accident 是不是不太好……
 */
@RequiredArgsConstructor
@Getter
final class FriendAccident implements HttpNotice {

	public enum Type {
		Moved,
		AbandonedMe,
		Inaccessible,
	}

	private final Type type;

	private final String name;
	private final URI url;

	private final URI newUrl;

	private final Instant time;

	@Override
	public String getKind() {
		return "fr";
	}
}
