package net.kaciras.blog.domain.permission;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public final class PermissionSet {

	private String module;

	private Set<Permission> permissions;
}
