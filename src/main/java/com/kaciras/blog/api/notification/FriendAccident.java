package com.kaciras.blog.api.notification;

import java.net.URI;
import java.time.Instant;

public final class FriendAccident {

	public enum Type {
		AbandonedMe,
		Inaccessible,
	}

	public Type type;
	public String name;
	public URI url;
	public Instant time;
}
