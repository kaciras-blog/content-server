package net.kaciras.blog.api.misc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.RedisKeys;
import net.kaciras.blog.infra.principal.RequireAuthorize;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/recommendation/cards")
class CardsController {

	/**
	 * 卡片需要保证次序，并且支持删除、插入到任意位置，Redis内置的数据类型
	 * 不能很好地处理，故直接序列化整个列表。
	 */
	private final RedisTemplate<String, byte[]> redisTemplate;
	private final ObjectMapper objectMapper;

	@GetMapping
	public List<SlideCard> getPages() throws IOException {
		var encode = redisTemplate.opsForValue().get(RedisKeys.CardList.value());
		if (encode == null) {
			return Collections.emptyList();
		}
		return objectMapper.readValue(encode, objectMapper.getTypeFactory()
				.constructCollectionType(List.class, SlideCard.class));
	}

	/**
	 * 考虑到轮播通常不会有很多页，直接全量更新。
	 *
	 * @param cards 卡片列表
	 * @throws JsonProcessingException 如果出这异常，说明代码有BUG
	 */
	@RequireAuthorize
	@PutMapping
	public ResponseEntity<Void> update(@RequestBody @Valid List<SlideCard> cards) throws Exception {
		redisTemplate.opsForValue().set(RedisKeys.CardList.value(), objectMapper.writeValueAsBytes(cards));
		return ResponseEntity.noContent().build();
	}
}
