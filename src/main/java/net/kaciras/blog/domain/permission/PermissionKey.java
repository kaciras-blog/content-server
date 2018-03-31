package net.kaciras.blog.domain.permission;

import lombok.Data;

@Data
public final class PermissionKey {

	private final String module;
	private final String name;
}
