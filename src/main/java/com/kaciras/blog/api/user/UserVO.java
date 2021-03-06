package com.kaciras.blog.api.user;

import com.kaciras.blog.api.account.AuthType;
import com.kaciras.blog.infra.codec.ImageReference;

import java.time.Instant;

public final class UserVO {

	public int id;

	public String name;
	public ImageReference avatar;

	public AuthType auth;

	public Instant createTime;
}
