package net.kaciras.blog.api.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequestMapping("/config")
@RestController
class ConfigController {

	@GetMapping
	public void getProperties(List<String> keys) {

	}

	@PutMapping
	public void setProperties(Map<String, String> props) {

	}
}
