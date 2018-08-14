package net.kaciras.blog.pojo;

import lombok.Data;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

import java.io.Serializable;

@Data
public class UserVo implements Serializable {

	private int id;

	private String name;

	private ImageRefrence head;
}
