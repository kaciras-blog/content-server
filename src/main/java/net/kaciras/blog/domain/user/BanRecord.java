package net.kaciras.blog.domain.user;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
@Data
final class BanRecord {

	private int id;

	private LocalDateTime start;
	private LocalDateTime end;
	private int operator;
	private String cause;
}
