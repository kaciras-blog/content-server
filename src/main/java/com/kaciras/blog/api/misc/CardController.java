package com.kaciras.blog.api.misc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.RedisKeys;
import com.kaciras.blog.infra.principal.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cards")
class CardController {

	/**
	 * 卡片需要保证次序，并且支持删除、插入到任意位置，
	 * Redis 内置的数据类型不能很好地处理，故直接序列化整个列表。
	 */
	private final RedisTemplate<String, byte[]> redisTemplate;

	private final ObjectMapper objectMapper;

	@GetMapping
	public List<Card> getPages() throws IOException {
		var json = redisTemplate.opsForValue().get(RedisKeys.CardList.value());
		if (json == null) {
			return Collections.emptyList();
		}
		return objectMapper.readValue(json, objectMapper.getTypeFactory()
				.constructCollectionType(List.class, Card.class));
	}

	/**
	 * 考虑到卡片不会有很多，直接全量更新。
	 *
	 * @param cards 卡片列表
	 * @throws JsonProcessingException 如果出这异常，说明代码有BUG
	 */
	@RequirePermission
	@PutMapping
	public ResponseEntity<Void> update(@RequestBody @Valid List<Card> cards) throws Exception {
		redisTemplate.opsForValue().set(RedisKeys.CardList.value(), objectMapper.writeValueAsBytes(cards));
		return ResponseEntity.noContent().build();
	}
}
