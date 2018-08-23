package net.kaciras.blog.message;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import java.time.LocalDateTime;

//系统消息 OR 私信
@EqualsAndHashCode(of = "id")
@Setter(AccessLevel.PACKAGE)
@Data
public class Message {

	private int id;
	private String title;
	private String content;

	private int sender;
	private LocalDateTime createdTime;
}
