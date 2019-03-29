package net.kaciras.blog.api.user;

import lombok.*;
import net.kaciras.blog.api.principle.AuthType;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.net.InetAddress;
import java.time.LocalDateTime;

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
	private ImageRefrence head;

	private boolean deleted;

	private AuthType authType;
	private LocalDateTime registerTime;
	private InetAddress registerIP;

	private User(int id, String name) {
		this.id = id;
		this.name = name;
	}
}
