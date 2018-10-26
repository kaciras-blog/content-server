package net.kaciras.blog.api.user;

import lombok.Data;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

@Data
public class UserVo {

	private int id;
	private String name;
	private ImageRefrence head;
}
