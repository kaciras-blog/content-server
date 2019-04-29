package net.kaciras.blog.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RequiredArgsConstructor
@RequestMapping("/config")
@RestController
class ConfigController {

	private final ConfigService configService;
	private final ObjectMapper objectMapper;

	@GetMapping("/{name}")
	public Object getProperties(@PathVariable String name) {
		return configService.get(name);
	}

	@RequireAuthorize
	@PatchMapping("/{name}")
	public void setProperties(HttpServletRequest request, @PathVariable String name) throws IOException {
		var config = configService.get(name);
		objectMapper.readerForUpdating(config).readValue(request.getReader());
		configService.set(name, config);
	}
}
