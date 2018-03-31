package net.kaciras.blog.domain.permission;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "name")
public final class Permission {

	private final String name;

	private final String desc;
}
