package net.kaciras.blog.recommend;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.collections.DefaultRedisMap;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/recommendation/swiper")
public class SwiperController {

	private final RedisMap<Object, SwipPage> swipPages;

	@SuppressWarnings("unchecked")
	public SwiperController(RedisTemplate<String, Object> template) {
		this.swipPages = new DefaultRedisMap("swiper", template);
	}

	@GetMapping
	public RedisMap<Object, SwipPage> getPages() {
		return swipPages;
	}

	@PutMapping("/{name}")
	public ResponseEntity<Void> putPage(@PathVariable String name,
										@RequestBody SwipPage page) throws URISyntaxException {
		if(swipPages.put(name, page) != null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.created(new URI("/recommendation/swiper/" + name)).build();
	}

	@DeleteMapping("/{name}")
	public ResponseEntity<Void> deletePage(@PathVariable String name) {
		swipPages.remove(name);
		return ResponseEntity.noContent().build();
	}
}
