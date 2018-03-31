package net.kaciras.blog.domain.user;

import lombok.Data;

import java.net.InetAddress;
import java.time.LocalDateTime;

@Data
public final class LoginRecord {

	private InetAddress address;
	private LocalDateTime time;
}
