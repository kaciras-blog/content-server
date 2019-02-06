package net.kaciras.blog.api.user;

import lombok.*;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Data
@Configurable
public class User {

	public static final User GUEST = new User(0, "游客");

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserDAO userDAO;

	private int id;
	private String name;

	private String email;
	private ImageRefrence head;

	private boolean deleted;

	private User(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public void updateHead(ImageRefrence head) {
		this.head = head;
		userDAO.updateHead(head);
	}
}
