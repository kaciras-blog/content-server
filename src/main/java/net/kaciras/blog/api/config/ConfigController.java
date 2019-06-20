package net.kaciras.blog.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
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

	@RequireAuthorize
	@PatchMapping
	public ResponseEntity<Object> setProperties(@PathVariable String name, Reader body) throws IOException {
		var config = configBindingManager.get(name);
		if (config == null) {
			return ResponseEntity.notFound().build();
		}
		objectMapper.readerForUpdating(config).readValue(body);
		configBindingManager.set(name, config);
		return ResponseEntity.ok(config);
	}
}
