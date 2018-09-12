package net.kaciras.blog.api.message;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Setter(AccessLevel.PACKAGE)
@Data
public class MessageReceiver {

	private int userId;
	private boolean readed;
}
