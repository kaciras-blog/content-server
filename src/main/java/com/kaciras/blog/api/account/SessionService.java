package com.kaciras.blog.api.account;

import com.kaciras.blog.api.SessionAttributes;
import com.kaciras.blog.infra.autoconfigure.SessionCookieProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public final class SessionService {

	private static final String CSRF_COOKIE_NAME = "CSRF-Token";

	private final SessionCookieProperties cookieProperties;

	public void putUser(HttpServletRequest request, HttpServletResponse response, int id, boolean remember) {
		var session = request.getSession(true);
		session.setAttribute(SessionAttributes.USER_ID, id);

		var csrfToken = UUID.randomUUID().toString();
		var csrfCookie = new Cookie(CSRF_COOKIE_NAME, csrfToken);
		csrfCookie.setPath("/");
		csrfCookie.setDomain(cookieProperties.getDomain());
		csrfCookie.setSecure(true);

		if (remember) {
			csrfCookie.setMaxAge(session.getMaxInactiveInterval());
		}
		response.addCookie(csrfCookie);
	}
}
