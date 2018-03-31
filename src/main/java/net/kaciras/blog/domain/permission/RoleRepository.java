package net.kaciras.blog.domain.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.kaciras.blog.domain.Utils.*;
import static net.kaciras.blog.domain.permission.Role.*;

@RequiredArgsConstructor
@Repository
public class RoleRepository {

	private static final int PERM_NAME_LENGTH = 255;
	private static final Pattern ALLOW_CHARS = Pattern.compile("^[0-9a-zA-Z_]+$");

	private final RolePermissionDAO permAssDAO;
	private final RoleDAO roleDAO;
	private final UserRoleDAO userRoleDAO;

	public int add(String name) {
		Role role = new Role(name);
		roleDAO.insert(role);
		return role.getId();
	}

	@Deprecated
	public void add(Role role) {
		roleDAO.insertManually(role);
	}

	public void remove(int id) {
		if (id == ANYONE_ROLE_ID || id == DEFAULT_USER_ROLE_ID || id == ADMIN_ROLE_ID) {
			throw new IllegalArgumentException("无法修改内置角色");
		}
		checkEffective(roleDAO.delete(id));
	}

	public void update(Role role) {
		checkEffective(roleDAO.updateAttribute(role));
	}

	@SuppressWarnings("unchecked")
	public Role get(int id) {
		Role role = checkNotNullResource(roleDAO.selectAttribute(id));
		role.setPermissionSet(permAssDAO.selectByRoleId(id));
		role.setIncludes((List<Role>) Enhancer.create(List.class, new RoleIncludesLoader(this, roleDAO, id)));
		return role;
	}

	@Transactional
	public void changeIncludes(int id, List<Integer> includes) {
		roleDAO.deleteIncludes(id);
		for (int include : includes) {
			/*
			 * 这里检查了被包含者不能包含包含者，也就是循环引用问题。
			 * 虽然在上层实现上可以采用一些方法避免死循环，但是在逻辑上
			 * 这样的循环引用不太合理。
			 * 这个检测需要循环 + 递归，效率上比较差。
			 */
			if (included(include, id)) {
				throw new IllegalArgumentException("重复包含：要包含的角色id=" + include + "反而包含了其包含者");
			}
			roleDAO.insertInclude(id, include);
		}
	}

	/**
	 * 检查是否存在循环包含（Include），例如A包含B，B又包含A。
	 * 方法需要递归，在包含关系复杂的情况下可能比较慢，但角色修改
	 * 的频率并不高，有一些延迟也是可以接受的。
	 *
	 * @param base   递归起始角色id
	 * @param target 待检查的id
	 * @return 如果已包含则返回true，否则false
	 */
	private boolean included(int base, int target) {
		for (int role : roleDAO.selectIncludes(base)) {
			if (role == target || included(role, target))
				return true;
		}
		return false;
	}

	public List<Role> findAll(Integer userId) {
		if (userId == null) {
			return Collections.singletonList(get(Role.ANYONE_ROLE_ID));
		}
		List<Role> roleSet = userRoleDAO.selectUserRoles(userId).stream()
				.map(this::get)
				.collect(Collectors.toList());
		roleSet.add(get(Role.ANYONE_ROLE_ID));
		if (userId > 0) {
			roleSet.add(get(Role.DEFAULT_USER_ROLE_ID));
		}
		return roleSet;
	}

	public void addPermissionToRole(int roleId, String group, String name) {
		permAssDAO.insert(roleId, convertName(group, name));
	}

	public void deletePermissionFromRole(int roleId, String group, String name) {
		permAssDAO.delete(roleId, convertName(group, name));
	}

	private String convertName(String group, String name) {
		if (group == null || name == null) {
			throw new NullPointerException("权限参数不能为null");
		}
		if (!ALLOW_CHARS.matcher(name).find() || !ALLOW_CHARS.matcher(group).find()) {
			throw new IllegalArgumentException("权限名和组名仅支持0-9a-zA-Z_");
		}

		String combied = group + "#" + name;
		if (combied.isEmpty() || combied.length() > PERM_NAME_LENGTH) {
			throw new IllegalArgumentException("权限名和组名总共长度必须在1-255个字符之间");
		}
		return combied;
	}

	public void removeAllFromRole(int roleId) {
		checkPositive(roleId, "roleId");
		permAssDAO.deleteById(roleId);
	}
}
