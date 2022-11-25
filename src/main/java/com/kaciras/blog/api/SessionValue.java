package com.kaciras.blog.api;

import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

/**
 * 常量类，包含本应用里定义的 HTTP 会话所有属性的名字。
 * 因为 HTTP 会话属于公共的功能，可能被多处调用，所以把所有用到的属性统一写在此处。
 * <p>
 * HttpSession 能够存储任意类型的对象，但每个属性都有固定的类型，
 * 所以除了属性名外，类型信息也是需要确定的。用法如下：
 *
 * <pre>
 * var captcha = SessionValue.CAPTCHA.getFrom(session);
 * SessionValue.CAPTCHA.removeFrom(session);
 * SessionValue.CAPTCHA.setTo(session, captcha);
 * </pre>
 *
 * 该类要写在最前，看起来有点怪。做成扩展方法也许不错，但这里不是 C#，Lombok 的那玩意我也没看懂。
 *
 * <h2>为何不用枚举</h2>
 * 枚举不支持泛型，曾经有个提案但是被否决了，相关讨论见
 * <a href="https://bugs.openjdk.java.net/browse/JDK-8170351">JDK-8170351</a>
 *
 * <h2>局限性</h2>
 * 如果类型不是 public 的，那么就无法在这里访问，不过这也意味着只能在同一个包内使用，没必要写在此处。
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
