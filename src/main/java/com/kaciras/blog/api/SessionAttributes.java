package com.kaciras.blog.api;

import lombok.experimental.UtilityClass;

/**
 * 常量类，包含 HTTP 会话所有属性的名字。
 *
 * <h2>为何不用枚举</h2>
 * 如果用枚举，那么调用代码会是 {@code session.get(SessionAttributes.CAPTCHA.value())}
 * 最后的 .value() 纯属多余，不仅写的麻烦，还降低了可读性。
 */
@UtilityClass
public class SessionAttributes {

	public String CAPTCHA = "Captcha";

	public String CAPTCHA_TIME = "CapTime";

	public String USER_ID = "UserId";
}
