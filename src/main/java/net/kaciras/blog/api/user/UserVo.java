package net.kaciras.blog.api.user;

import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.api.principal.AuthType;
import net.kaciras.blog.infrastructure.codec.ImageReference;

import java.time.Instant;

@Getter
@Setter
public final class UserVo {

	private int id;
	private AuthType authType;

	private String name;
	private ImageReference head;

	private Instant registerTime;
}
