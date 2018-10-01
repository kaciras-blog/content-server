package net.kaciras.blog.api;

import net.kaciras.blog.api.perm.WebPrincipal;
import net.kaciras.blog.infrastructure.exception.PermissionException;
import org.springframework.stereotype.Component;

@Component
public final class SecurityContext {

	private static final ThreadLocal<WebPrincipal> threadLocalUser = new ThreadLocal<>();

	static void setPrincipal(WebPrincipal principal) {
		threadLocalUser.set(principal);
	}

	public static WebPrincipal getPrincipal() {
		return threadLocalUser.get();
	}

	/**
	 * 检查当前的用户是否不是参数id所指定的用户。
	 * 因为一般当前用户和所需用户不同的情况才需要额外处理，所以此方法是不相同返回true。
	 *
	 * @param id 用户id
	 * @return 如果当前用户不存在，或用户id与参数指定的id不同则返回true，否则false
	 */
	public static boolean isNotUser(int id) {
		return getPrincipal().getId() != id;
	}

	public static void requireId(int id) {
		if (isNotUser(id)) throw new PermissionException();
	}

	public static void requireLogin() {
		if (getPrincipal().isAnynomous()) throw new PermissionException();
	}

	public static int getUserId() {
		return getPrincipal().getId();
	}

	public static boolean checkSelf(int id, String perm) {
		var principal = getPrincipal();
		return principal.getId() == id || principal.hasPermission(perm);
	}

	public static void requireSelf(int id, String perm) {
		if (!checkSelf(id, perm)) {
			throw new PermissionException();
		}
	}

	public static void require(String perm) {
		if (!getPrincipal().hasPermission(perm))
			throw new PermissionException();
	}
}
