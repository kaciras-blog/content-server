package net.kaciras.blog.api.misc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/recommendation/swiper")
class SwiperController {

	/**
	 * 轮播需要保证次序，并且支持删除、插入到任意位置，Redis内置的数据类型
	 * 不能很好地处理，故直接序列化整个列表。
	 */
	private final RedisTemplate<String, byte[]> redisTemplate;
	private final ObjectMapper objectMapper;

	@GetMapping
	public Object getPages() throws IOException {
		var encode = redisTemplate.opsForValue().get("swiper");
		if (encode == null) {
			return Collections.emptyList();
		}
		return objectMapper.readValue(encode, objectMapper.getTypeFactory()
				.constructCollectionType(List.class, SwiperSlide.class));
	}

	//考虑到轮播通常不会有很多页，直接全量更新。
	@RequireAuthorize
	@PutMapping
	public ResponseEntity<Void> update(@RequestBody List<SwiperSlide> slides) throws JsonProcessingException {
		redisTemplate.opsForValue().set("swiper", objectMapper.writeValueAsBytes(slides));
		return ResponseEntity.noContent().build();
	}
}
