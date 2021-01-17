package com.kaciras.blog.infra.principal;

import com.kaciras.blog.infra.exception.PermissionException;

/**
 * 把用户身份保存在线程本地变量里，方便随时获取，跟 Spring Security 里差不多的东西。
 * 除此之外，还带有一些便捷方法。
 */
public final class SecurityContext {

	private SecurityContext() {}

	private static final ThreadLocal<WebPrincipal> threadLocal = new ThreadLocal<>();

	public static void setPrincipal(WebPrincipal principal) {
		threadLocal.set(principal);
	}

	/** 需要添加 SecurityContextFilter 后才能使用 SecurityContext */
	public static WebPrincipal getPrincipal() {
		return threadLocal.get();
	}

	/* ==================================== Helper Methods ==================================== */

	public static int getUserId() {
		return getPrincipal().getId();
	}

	public static void require(String perm) {
		if (!getPrincipal().hasPermission(perm)) throw new PermissionException();
	}

	/**
	 * 检查当前的用户是否不是参数 id 所指定的用户。
	 * 因为一般当用户和所需用户不同时才需要额外处理，所以设计为反义方法。
	 *
	 * @param id 用户id
	 * @return 如果当前用户不存在，或用户id与参数指定的id不同则返回true，否则false
	 */
	public static boolean isNot(int id) {
		return getPrincipal().getId() != id;
	}

	public static void requireId(int id) {
		if (isNot(id)) throw new PermissionException();
	}

	public static void requireLogin() {
		if (getPrincipal().isAnonymous()) throw new PermissionException();
	}
}
