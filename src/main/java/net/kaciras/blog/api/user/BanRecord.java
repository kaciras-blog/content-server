package net.kaciras.blog.api.user;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Accessors(chain = true)
@EqualsAndHashCode(of = "id")
@Data
public final class BanRecord {

	private int id;

	private LocalDateTime start;
	private LocalDateTime end;
	private int operator;
	private String cause;

	private UnbanRecord unbanRecord;
}
