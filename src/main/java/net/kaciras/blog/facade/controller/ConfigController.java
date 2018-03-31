package net.kaciras.blog.facade.controller;

import net.kaciras.blog.domain.config.ConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/configs")
public final class ConfigController {

	private final ConfigService configService;

	public ConfigController(ConfigService configService) {
		this.configService = configService;
	}

	@PutMapping("/{name}")
	public ResponseEntity putConfig(@PathVariable String name, @RequestParam String value) {
		configService.set(name, value);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public Map getConfigs() {
		return configService.getModifiable();
	}
}
