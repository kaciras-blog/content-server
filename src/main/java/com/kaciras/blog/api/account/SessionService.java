package com.kaciras.blog.api.account;

import com.kaciras.blog.api.SessionValue;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SessionService {

	public static final String REMEMBER_ME_ATTR = "remember-me";

	private final HttpSessionTable sessionTable;

	public void putUser(HttpServletRequest request, int uid, boolean remember) {
		var session = request.getSession(true);

		/*
		 * 重新登陆时刷新下 ID，以便更改 Cookie 的过期时间。
		 *
		 * 考虑这种情况：
		 * 登录（记住）、30 天之后令牌过期、登录（不记住）。
		 * 第二次登录时已有一个长期的 Cookie，由于 Spring Session 无法修改现有的 age，
		 * 导致即使未选择记住但仍长期保持了登录。
		 *
		 * 【关于跟踪 Cookie】
		 * 为了实现不记住登录，必须使用会话生命周期的 Cookie，如果需要长期的会话，
		 * 比如对客户端做跟踪统计，则必须分离出单独的 Cookie。
		 */
		if (!session.isNew()) {
			request.changeSessionId();
		}

		SessionValue.USER_ID.setTo(session, uid);
		sessionTable.add(uid, session.getId());

		// 使用 Spring Session 默认的机制来记住登录状态。
		// org.springframework.session.web.http.DefaultCookieSerializer#getMaxAge
		if (remember) {
			request.setAttribute(REMEMBER_ME_ATTR, true);
		}
	}
}
