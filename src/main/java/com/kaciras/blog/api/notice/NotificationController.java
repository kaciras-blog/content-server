package com.kaciras.blog.api.notice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.infra.principal.RequirePermission;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequirePermission
@RequiredArgsConstructor
@RestController
@RequestMapping("/notifications")
class NotificationController {

	private final NotificationService service;
	private final ObjectMapper objectMapper;

	@GetMapping
	public void getAll(HttpServletResponse response) throws IOException {
		var result = service.getAll();
		response.setStatus(200);
		@Cleanup var gen = objectMapper.createGenerator(response.getOutputStream());

		gen.writeStartObject();
		for (var e : result.entrySet()) {
			gen.writeFieldName(e.getKey());
			gen.writeStartArray();
			for (var json : e.getValue()) {
				gen.writeRawUTF8String(json, 0, json.length);
			}
			gen.writeEndArray();
		}
		gen.writeEndObject();
	}

	@DeleteMapping
	public ResponseEntity<Void> clear() {
		service.clear();
		return ResponseEntity.noContent().build();
	}
}
