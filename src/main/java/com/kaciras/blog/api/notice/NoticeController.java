package com.kaciras.blog.api.notice;

import com.kaciras.blog.infra.principal.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequirePermission
@RequiredArgsConstructor
@RestController
@RequestMapping("/notifications")
class NoticeController {

	private final NoticeService service;

	@GetMapping
	public List<Notice> getAll() {
		return service.getAll();
	}

	@DeleteMapping
	public ResponseEntity<Void> clear() {
		service.clear();
		return ResponseEntity.noContent().build();
	}
}
