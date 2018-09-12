package net.kaciras.blog.api.user;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter(AccessLevel.PACKAGE)
@Data
public final class UnbanRecord {

	private int operator;
	private String cause;
	private LocalDateTime time;
}
