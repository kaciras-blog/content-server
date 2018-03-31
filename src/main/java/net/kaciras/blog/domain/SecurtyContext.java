package net.kaciras.blog.domain;

import net.kaciras.blog.domain.permission.PermissionKey;
import net.kaciras.blog.domain.permission.RoleService;
import net.kaciras.blog.infrastructure.exception.PermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class SecurtyContext {

	private static final ThreadLocal<Integer> threadLocalUser = new ThreadLocal<>();

	private static boolean debugPermission;

	@Value("${permission.debugMode}")
	public void setDebugPermission(boolean debugPermission) {
		SecurtyContext.debugPermission = debugPermission;
	}

	public static void setCurrentUser(Integer userId) {
		threadLocalUser.set(userId);
	}

	public static Integer getCurrentUser() {
		if (debugPermission) {
			return 1;
		}
		return threadLocalUser.get();
	}

	public static int getRequiredCurrentUser() {
		Integer userDTO = getCurrentUser();
		if (userDTO == null) {
			throw new PermissionException();
		}
		return userDTO;
	}

	/**
	 * 暂时采用单体架构，不解决分布权限问题，故搞了下面这几个方法快速检查权限
	 *
	 * @since 2018-1-28
	 */
	private static RoleService service;

	@Autowired
	public void setUserRoleSerivce(RoleService service) {
		SecurtyContext.service = service;
	}

	public static void checkAccept(String group, String name) {
		if (!accept(group, name)) throw new PermissionException();
	}

	public static boolean accept(String group, String name) {
		return service.accept(SecurtyContext.getCurrentUser(), new PermissionKey(group, name));
	}
}
