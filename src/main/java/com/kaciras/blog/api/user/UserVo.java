package com.kaciras.blog.api.user;

import com.kaciras.blog.api.principal.AuthType;
import com.kaciras.blog.infra.codec.ImageReference;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public final class UserVo {

	private int id;
	private AuthType authType;

	private String name;
	private ImageReference avatar;

	private Instant registerTime;
}
