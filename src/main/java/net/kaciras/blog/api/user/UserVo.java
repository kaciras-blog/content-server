package net.kaciras.blog.api.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.api.principle.AuthType;
import net.kaciras.blog.infrastructure.codec.ImageReference;

import java.time.LocalDateTime;

@Getter
@Setter
public final class UserVo {

	private int id;
	private AuthType authType;

	private String name;
	private ImageReference head;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime registerTime;
}
