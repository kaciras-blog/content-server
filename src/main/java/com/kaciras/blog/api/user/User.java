package com.kaciras.blog.api.user;

import com.kaciras.blog.api.account.AuthType;
import com.kaciras.blog.infra.codec.ImageReference;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.InetAddress;
import java.time.Instant;

@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Getter
@Setter
public final class User {

	// 数据库里也有适配
	public static final User GUEST = new User(0, "(游客)", ImageReference.parse("akalin.jpg"));

	private int id;

	private String name;
	private ImageReference avatar;

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
