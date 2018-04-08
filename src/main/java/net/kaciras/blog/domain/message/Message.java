package net.kaciras.blog.domain.message;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import java.time.LocalDateTime;

@EqualsAndHashCode(of = "id")
@Setter(AccessLevel.PACKAGE)
@Data
public class Message {

	private int id;
	private String title;
	private String content;

	private int sender;
	private int level;
	private LocalDateTime time;
}
