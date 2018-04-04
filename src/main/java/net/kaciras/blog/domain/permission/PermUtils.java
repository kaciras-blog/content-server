package net.kaciras.blog.domain.permission;

import java.util.regex.Pattern;

final class PermUtils {

	private static final int PERM_NAME_LENGTH = 255;

	private static final Pattern ALLOW_CHARS = Pattern.compile("^[0-9a-zA-Z_]+$");

	static String convertName(String group, String name) {
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

	private PermUtils() {}
}
