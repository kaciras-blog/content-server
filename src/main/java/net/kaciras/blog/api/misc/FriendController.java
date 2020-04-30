package net.kaciras.blog.api.misc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.kaciras.blog.api.RedisKeys;
import net.kaciras.blog.infra.exception.RequestArgumentException;
import net.kaciras.blog.infra.principal.RequireAuthorize;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collection;
import java.util.stream.Collectors;

import static net.kaciras.blog.infra.func.FunctionUtils.unchecked;

/**
 * TODO: 考虑做自动申请友链
 */
@SuppressWarnings("ConstantConditions")
@RestController
@RequestMapping("/friends")
class FriendController {

	private final BoundHashOperations<String, String, byte[]> redisHash;
	private final ObjectMapper objectMapper;

	FriendController(RedisTemplate<String, byte[]> redis, ObjectMapper objectMapper) {
		this.redisHash = redis.boundHashOps(RedisKeys.Friends.value());
		this.objectMapper = objectMapper;
	}

	// lambda 显示写出参数类型才能让编译器推导返回类型？
	@GetMapping
	public Collection<FriendLink> getFriends() {
		return redisHash.entries().values()
				.stream()
				.map(unchecked((byte[] value) -> objectMapper.readValue(value, FriendLink.class)))
				.collect(Collectors.toList());
	}

	@RequireAuthorize
	@PostMapping
	public ResponseEntity<FriendLink> makeFriends(@RequestBody @Valid FriendLink input) throws JsonProcessingException {
		var host = URI.create(input.getUrl()).getHost();
		if (host == null) {
			throw new RequestArgumentException("友链的URL格式错误");
		}
		redisHash.put(host, objectMapper.writeValueAsBytes(input));
		return ResponseEntity.created(URI.create("/friends/" + host)).body(input);
	}

	@RequireAuthorize
	@DeleteMapping("/{host}")
	public ResponseEntity<Void> rupture(@PathVariable String host) {
		return redisHash.delete(host) > 0 ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
	}
}
