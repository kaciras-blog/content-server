package com.kaciras.blog.api.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/notifications")
class NotificationController {

	private final NotificationService service;

	@GetMapping
	void getAll() {
		service.getAll();
	}
}
