package net.kaciras.blog.api.recommend;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.perm.RequirePrincipal;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor
@RestController
@RequestMapping("/recommendation/swiper")
public class SwiperController {

	/**
	 * 轮播需要保证次序，并且支持删除、插入到任意位置，Redis内置的数据类型
	 * 不能很好地处理，故直接序列化整个列表。
	 */
	private final RedisTemplate<String, Object> redisTemplate;

	@GetMapping
	public Object getPages() {
		return redisTemplate.opsForValue().get("swiper");
	}

	//考虑到轮播通常不会有很多页，直接全量更新。
	@RequirePrincipal
	@PutMapping
	public ResponseEntity<Void> change(@RequestBody List<SwiperSlide> slides) {
		redisTemplate.opsForValue().set("swiper", slides);
		return ResponseEntity.noContent().build();
	}
}
