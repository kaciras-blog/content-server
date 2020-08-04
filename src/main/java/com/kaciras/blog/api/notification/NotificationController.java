package com.kaciras.blog.api.notification;

import com.kaciras.blog.infra.principal.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequirePermission
@RequiredArgsConstructor
@RestController
@RequestMapping("/notifications")
class NotificationController {

	private final NotificationRepository repository;

	@GetMapping
	public Notifications getAll() {
		return repository.getAll();
	}

	@DeleteMapping
	public ResponseEntity<Void> clear() {
		repository.clear();
		return ResponseEntity.noContent().build();
	}
}
