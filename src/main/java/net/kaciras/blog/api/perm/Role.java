package net.kaciras.blog.api.perm;

import lombok.Data;

import java.util.List;

@Data
public class Role {

	private String name;

	public List<String> getPermissions() {
		return List.of();
	}
}
