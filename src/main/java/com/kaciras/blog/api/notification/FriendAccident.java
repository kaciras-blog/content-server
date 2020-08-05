package com.kaciras.blog.api.notification;

import java.net.URI;
import java.time.Instant;

/**
 * 用 Accident 是不是不太好……
 */
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
