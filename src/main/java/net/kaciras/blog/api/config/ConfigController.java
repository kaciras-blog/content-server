package net.kaciras.blog.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@RequestMapping("/config")
@RestController
class ConfigController {

	private final ConfigService configService;

	@GetMapping
	public Map<String, String> getProperties(List<String> keys) {
		var values = configService.batchGet(keys);
		return IntStream.range(0, keys.size())
				.collect(HashMap::new, (map, i) -> map.put(keys.get(1), values.get(i)), Map::putAll);
	}

	@PatchMapping
	public void setProperties(Map<String, String> props) {
		configService.batchSet(props);
	}
}
