package com.kaciras.blog.api;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpSession;
import java.time.Instant;

/**
 * 常量类，包含本应用里定义的 HTTP 会话所有属性的名字。
 * 因为 HTTP 会话属于公共的功能，可能被多处调用，所以把所有用到的属性统一写在此处。
 *
 * HttpSession 能够存储任意类型的对象，但每个属性都有固定的类型，
 * 所以除了属性名外，类型信息也是需要确定的。
 *
 * <h2>为何不用枚举</h2>
 * 枚举不支持泛型，相关讨论见
 * <a href="https://bugs.openjdk.java.net/browse/JDK-8170351">JDK-8170351</a>
 *
 * <h2>扩展方法</h2>
 * 做成扩展方法看起来不错，但这里不是 C#，Lombok 的那玩意我也没看懂。
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SessionValue<T> {

	public static final SessionValue<String> CAPTCHA = new SessionValue<>("Captcha");

	public static final SessionValue<Instant> CAPTCHA_TIME = new SessionValue<>("CapTime");

	public static final SessionValue<Integer> USER_ID = new SessionValue<>("UserId");

	private final String name;

	@SuppressWarnings("unchecked")
	public T getFrom(HttpSession session) {
		return (T) session.getAttribute(name);
	}

	public void removeFrom(HttpSession session) {
		session.removeAttribute(name);
	}

	public void setTo(HttpSession session, T value) {
		session.setAttribute(name, value);
	}
}
