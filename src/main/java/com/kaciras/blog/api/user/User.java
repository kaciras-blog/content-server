package com.kaciras.blog.api.user;

import com.kaciras.blog.api.principal.AuthType;
import com.kaciras.blog.infra.codec.ImageReference;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.net.InetAddress;
import java.time.Instant;

@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Data
@Configurable
public final class User {

	/** 数据库里也有适配 */
	public static final User GUEST = new User(0, "(游客)", ImageReference.parse("akalin.jpg"));

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private UserDAO userDAO;

	private int id;

	private String name;
	private ImageReference head;

	private boolean deleted;

	private AuthType authType;
	private Instant registerTime;
	private InetAddress registerIP;

	private User(int id, String name, ImageReference head) {
		this.id = id;
		this.name = name;
		this.head = head;
	}
}
