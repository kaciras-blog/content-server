package net.kaciras.blog.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
