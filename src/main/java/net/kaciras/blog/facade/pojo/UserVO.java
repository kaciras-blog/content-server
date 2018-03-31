package net.kaciras.blog.facade.pojo;

import lombok.Data;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

import java.io.Serializable;

@Data
public class UserVO implements Serializable {

	private int id;

	private String name;

	private ImageRefrence head;
}
