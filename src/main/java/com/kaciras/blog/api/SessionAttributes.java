package com.kaciras.blog.api;

import lombok.experimental.UtilityClass;

/**
 * 常量类，包含会话所有属性的名字。
 * <p>
 * 为何不用枚举？因为会话属性以字符串作为键而不是枚举，如果用枚举，那么调用代码会是：
 * {@code session.get(SessionAttributes.CAPTCHA.value())}
 * 最后的 .value() 纯属多余，不仅写的麻烦，还降低了可读性。
 */
@UtilityClass
public class SessionAttributes {

	public final String CAPTCHA = "Captcha";
	public final String CAPTCHA_TIME = "CapTime";
	public final String USER_ID = "UserId";
}
