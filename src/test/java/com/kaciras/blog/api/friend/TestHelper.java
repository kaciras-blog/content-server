package com.kaciras.blog.api.friend;

import com.kaciras.blog.infra.codec.ImageReference;

import java.time.Instant;

final class TestHelper {

	private static final ImageReference image = ImageReference.parse("test.png");

	public static FriendLink createFriend(String name) {
		return createFriend(name, null, null);
	}

	public static FriendLink createFriend(String name, String friendPage, Instant time) {
		var url = "https://" + name;
		return new FriendLink(url, name, image, image, friendPage, time);
	}
}
