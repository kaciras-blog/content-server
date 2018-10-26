package net.kaciras.blog.api.user;

import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

@Getter
@Setter
public class UserVo {

	private int id;
	private String name;
	private ImageRefrence head;
}
