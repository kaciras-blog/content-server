package net.kaciras.blog.api.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.api.principle.AuthType;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserVo {

	private int id;
	private String name;
	private ImageRefrence head;

	private AuthType authType;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime registerTime;
}
