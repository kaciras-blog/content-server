package com.kaciras.blog.api.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.infra.exception.RequestArgumentException;
import com.kaciras.blog.infra.principal.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.Reader;

@RequiredArgsConstructor
@RequestMapping("/config/{name}")
@RestController
class ConfigController {

	private final ConfigBindingManager configBindingManager;
	private final ObjectMapper objectMapper;

	@GetMapping
	public ResponseEntity<?> getProperties(@PathVariable String name) {
		var config = configBindingManager.get(name);
		if (config == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(config);
	}

	@RequirePermission
	@PatchMapping
	public Object setProperties(@PathVariable String name, Reader body) throws IOException {
		var config = configBindingManager.get(name);
		if (config == null) {
			return ResponseEntity.notFound().build();
		}
		try {
			objectMapper.readerForUpdating(config).readValue(body);
			configBindingManager.set(name, config);
			return config;
		} catch (JsonProcessingException e) {
			throw new RequestArgumentException("数据格式错误");
		}
	}
}
