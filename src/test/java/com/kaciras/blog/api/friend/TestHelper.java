package com.kaciras.blog.api.friend;

import com.kaciras.blog.infra.codec.ImageReference;

import java.net.URI;
import java.time.Instant;

final class TestHelper {

	private static final ImageReference image = ImageReference.parse("test.png");

	public static FriendLink createFriend(String domain) {
		return createFriend(domain, null, null);
	}

	public static FriendLink createFriend(String domain, String friendPage, Instant time) {
		var url = URI.create("https://" + domain + "/index.html");
		var f = friendPage != null ? URI.create(friendPage) : null;
		return new FriendLink(url, domain, image, image, f, time);
	}
}
