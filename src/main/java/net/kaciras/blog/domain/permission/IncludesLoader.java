package net.kaciras.blog.domain.permission;

import org.springframework.cglib.proxy.LazyLoader;

import java.util.List;
import java.util.stream.Collectors;

class IncludesLoader implements LazyLoader {

	private final RoleRepository repository;
	private final RoleDAO dao;
	private final int roleId;

	IncludesLoader(RoleRepository repository, RoleDAO dao, int roleId) {
		this.repository = repository;
		this.dao = dao;
		this.roleId = roleId;
	}

	@Override
	public Object loadObject() {
		List<Integer> ids = dao.selectIncludes(roleId);
		return ids.stream().map(repository::get).collect(Collectors.toList());
	}
}
