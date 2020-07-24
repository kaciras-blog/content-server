package com.kaciras.blog.api.friend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.RedisKeys;
import com.kaciras.blog.infra.exception.RequestArgumentException;
import com.kaciras.blog.infra.principal.RequireAuthorize;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

import static com.kaciras.blog.infra.func.FunctionUtils.uncheckedFn;

/**
 * 【一致性的】
 * 假定博主不会在同一时间做多个操作，这样就只考虑检测任务的并发。
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/friends")
class FriendController {

	private final RedisTemplate<String, byte[]> redis;

	private final ObjectMapper objectMapper;
	private final Clock clock;

	@GetMapping
	public List<FriendLink> getFriends() {
		return redis.opsForList()
				.range(RedisKeys.Friends.of("list"), 0, -1)
				.stream()
				.map(uncheckedFn(value -> objectMapper.readValue(value, FriendLink.class)))
				.collect(Collectors.toList());
	}

	@RequireAuthorize
	@PostMapping
	public ResponseEntity<FriendLink> makeFriend(@RequestBody @Valid FriendLink input) throws JsonProcessingException {
		var host = URI.create(input.url).getHost();
		if (host == null) {
			throw new RequestArgumentException("友链的URL格式错误");
		}

		var now = clock.instant();
		input.setCreateTime(now);

		// 未检查重复的站点
		redis.opsForList()
				.rightPush(RedisKeys.Friends.of("list"), objectMapper.writeValueAsBytes(input));

		var validateRecord = new ValidateRecord(input.url, input.friendPage, now, 0);
		redis.opsForHash()
				.put(RedisKeys.Friends.of("vR"), host, objectMapper.writeValueAsBytes(validateRecord));

		return ResponseEntity.created(URI.create("/friends/" + host)).body(input);
	}


	// 仅用于排序，假定输入的友链都是已存在的
	@RequireAuthorize
	@PutMapping
	public void updateAll(@RequestBody List<FriendLink> input) {
		var jsons = input.stream()
				.map(uncheckedFn(objectMapper::writeValueAsBytes))
				.toArray();
		redis.opsForList().rightPushAll(RedisKeys.Friends.of("list"), (byte[][]) jsons);
	}

	@RequireAuthorize
	@DeleteMapping("/{host}")
	public ResponseEntity<Void> rupture(@PathVariable String host) throws URISyntaxException {
		var friends = getFriends();
		var i = 0;

		for (; i < friends.size(); i++) {
			var url = new URI(friends.get(i).url);
			if (url.getHost().equals(host)) break;
		}

		if (i >= friends.size()) {
			return ResponseEntity.notFound().build();
		}

		redis.opsForList().trim(RedisKeys.Friends.of("list"), i, i);
		redis.opsForHash().delete(RedisKeys.Friends.of("vR"), host);

		return ResponseEntity.noContent().build();
	}
}
