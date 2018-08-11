package net.kaciras.blog.facade.controller;

import net.kaciras.blog.domain.config.ConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/configs")
final class ConfigController {

	private final ConfigService configService;

	public ConfigController(ConfigService configService) {
		this.configService = configService;
	}

	@PutMapping("/**")
	public ResponseEntity putConfig(HttpServletRequest request) {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		path = path.substring("/configs/".length()).replace('/', '.');
		configService.set(path, request.getParameter("value"));
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public Map getConfigs() {
		return configService.getModifiable();
	}
}
