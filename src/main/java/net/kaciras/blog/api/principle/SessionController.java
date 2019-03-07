package net.kaciras.blog.api.principle;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.SessionAttrNames;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RequiredArgsConstructor
@RestController
@RequestMapping("/session")
class SessionController {

	@GetMapping
	public int getUserId() {
		return SecurityContext.getUserId();
	}

	@DeleteMapping("/account")
	public ResponseEntity<Void> logout(HttpSession session) {
		session.removeAttribute(SessionAttrNames.USER_ID);
		return ResponseEntity.status(HttpStatus.RESET_CONTENT).build();
	}
}
