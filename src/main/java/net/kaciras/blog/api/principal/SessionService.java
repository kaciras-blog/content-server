package net.kaciras.blog.api.principal;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.SessionAttributes;
import net.kaciras.blog.infrastructure.autoconfig.SessionCookieProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SessionService {

	private static final String CSRF_COOKIE_NAME = "CSRF-Token";

	private final SessionCookieProperties cookieProperties;

	public void putUser(HttpServletRequest request, HttpServletResponse response, int id, boolean remember) {
		var session = request.getSession(true);
		session.setAttribute(SessionAttributes.USER_ID, id);

		var csrfToken = UUID.randomUUID().toString();
		var csrfCookie = new Cookie(CSRF_COOKIE_NAME, csrfToken);
		csrfCookie.setPath("/");
		csrfCookie.setDomain(cookieProperties.getDomain());

		if (remember) {
			csrfCookie.setMaxAge(session.getMaxInactiveInterval());
		}
		response.addCookie(csrfCookie);
	}
}
