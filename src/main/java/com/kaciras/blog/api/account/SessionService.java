package com.kaciras.blog.api.account;

import com.kaciras.blog.api.SessionValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@Service
public class SessionService {

	public static final String REMEMBER_ME_ATTR = "remember-me";

	private final HttpSessionTable sessionTable;

	public void putUser(HttpServletRequest request, int id, boolean remember) {
		var session = request.getSession(true);
		SessionValue.USER_ID.setTo(session, id);
		sessionTable.add(id, session.getId());

		if (remember) {
			request.setAttribute(REMEMBER_ME_ATTR, true);
		}
	}
}
