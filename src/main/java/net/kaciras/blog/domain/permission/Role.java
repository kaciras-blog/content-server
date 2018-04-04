package net.kaciras.blog.domain.permission;

import lombok.*;
import net.kaciras.blog.infrastructure.event.role.RoleIncludeChangedEvent;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name"})
@NoArgsConstructor
@Getter
@Setter(AccessLevel.PACKAGE)
public class Role {

	static final int ANYONE_ROLE_ID = 1;
	static final int DEFAULT_USER_ROLE_ID = 2;
	static final int ADMIN_ROLE_ID = 3;

	@Qualifier("userPermissionCache")
	static Cache cache;

	static RolePermissionDAO rolePermissionDAO;

	static MessageClient messageClient;

// * - * - * - * - * - * - * - * - * - * - * - * - * - * -

	private int id;
	private String name;

	private List<Role> includes;

	private Set<PermissionKey> permissionSet;
	//搞个PagerCollection<UserId>做分页懒加载？

	Role(String name) {
		this.name = name;
	}

	Role(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public void addPermission(PermissionKey perm) {
		if (!permissionSet.add(perm)) {
			throw new IllegalArgumentException();
		}
		rolePermissionDAO.insert(id, PermUtils.convertName(perm.getModule(), perm.getName()));
	}

	public void removePermission(PermissionKey perm) {
		if (!permissionSet.remove(perm)) {
			throw new IllegalArgumentException();
		}
		rolePermissionDAO.delete(id, PermUtils.convertName(perm.getModule(), perm.getName()));
	}

	public boolean accept(PermissionKey pk) {
		return checkResurce(pk, this);
	}

	/**
	 * 检验角色及其所包含的角色是否具有指定的权限。
	 * <p>
	 * 因为角色不存在循环包含，所以在一次递归的路径上不存在相同的角色，而
	 * 递归返回时会添加缓存，另一次递归时遇到相同的角色必定在缓存中，无需
	 * 额外检查角色是否被重复校验。
	 *
	 * @param pk   权限标识
	 * @param role 角色
	 * @return 角色是否有指定的权限
	 */
	private boolean checkResurce(PermissionKey pk, Role role) {
		int cacheHash = Objects.hash(pk, role.id);
		Boolean cached = cache.get(cacheHash, Boolean.class);
		if (cached != null) {
			return cached;
		} else if (role.getId() == ADMIN_ROLE_ID) {
			return true;
		}
		boolean accept = permissionSet.contains(pk);
		if (!accept) {
			accept = role.getIncludes().stream().anyMatch(r -> checkResurce(pk, r));
		}
		cache.put(cacheHash, accept);
		return accept;
	}

	public void changeIncludes(List<Role> ids) {
		includes = ids;
		List<Integer> old = getIncludes().stream()
				.map(Role::getId)
				.collect(Collectors.toList());
		List<Integer> New = ids.stream()
				.map(Role::getId)
				.collect(Collectors.toList());
		RoleIncludeChangedEvent event = new RoleIncludeChangedEvent(id, old, New);
		messageClient.send(event);
	}
}
