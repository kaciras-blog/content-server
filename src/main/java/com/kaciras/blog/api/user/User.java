package com.kaciras.blog.api.user;

import com.kaciras.blog.api.account.AuthType;
import com.kaciras.blog.infra.codec.ImageReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.InetAddress;
import java.time.Instant;

@NoArgsConstructor
@Getter
@Setter
public final class User {

	public static final User GUEST = new User(0, "(游客)", null);

	private int id;

	private String name;
	private ImageReference avatar;
	private String email;

	private boolean deleted;

	private AuthType auth;
	private Instant createTime;
	private InetAddress createIP;

	private User(int id, String name, ImageReference avatar) {
		this.id = id;
		this.name = name;
		this.avatar = avatar;
	}
}
