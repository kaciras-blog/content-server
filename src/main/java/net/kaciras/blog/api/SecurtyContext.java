package net.kaciras.blog.api;

import net.kaciras.blog.infrastructure.exception.PermissionException;
import org.springframework.stereotype.Component;

@Component
public final class SecurtyContext {

	private static final ThreadLocal<Integer> threadLocalUser = ThreadLocal.withInitial(() -> 0);

	public static void setCurrentUser(Integer userId) {
		threadLocalUser.set(userId);
	}

	public static Integer getCurrentUser() {
		return threadLocalUser.get();
	}

	/**
	 * 检查当前的用户是否不是参数id所指定的用户。
	 * 因为getCurrentUser()可能返回null而无法直接与int类型比较，故定义此快捷方法。
	 * 因为一般当前用户和所需用户不同的情况才需要额外处理，所以此方法是不相同返回true。
	 *
	 * @param id 用户id
	 * @return 如果当前用户不存在，或用户id与参数指定的id不同则返回true，否则false
	 */
	public static boolean isNotUser(int id) {
		return getCurrentUser() != id;
	}

	public static void requireUser(int id) {
		if(isNotUser(id)) throw new PermissionException();
	}

	public static void requireLogin() {
		if(getCurrentUser() == 0) throw new PermissionException();
	}
}
