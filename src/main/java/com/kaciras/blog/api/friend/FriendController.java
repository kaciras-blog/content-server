package com.kaciras.blog.api.friend;

import com.kaciras.blog.infra.exception.ResourceStateException;
import com.kaciras.blog.infra.principal.RequireAuthorize;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.time.Clock;
import java.util.List;
import java.util.Map;

/**
 * 【一致性的讨论】
 * 假定博主不会在同一时间做多个修改操作，这样就只考虑检测任务的并发。
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/friends")
class FriendController {

	private final RedisMap<String, FriendLink> friendMap;
	private final RedisList<String> hostList;

	private final RedisMap<String, ValidateRecord> validateMap;

	private final Clock clock;

	// SpringDataRedis是真的垃圾……
	@GetMapping
	public FriendLink[] getFriends() {
		var list = friendMap.getOperations().execute(new SessionCallback<List<?>>() {
			@Override
			public List<?> execute(@NonNull RedisOperations operations) {
				operations.multi();
				operations.opsForList().range(hostList.getKey(), 0, -1);
				operations.opsForHash().entries(friendMap.getKey());
				return operations.exec();
			}
		});
		return generateCache((List<String>) list.get(0), (Map<String, FriendLink>) list.get(1));
	}

	private FriendLink[] generateCache(List<String> hosts, Map<String, FriendLink> map) {
		return hosts.stream().map(map::get).toArray(FriendLink[]::new);
	}

	@RequireAuthorize
	@PostMapping
	public ResponseEntity<FriendLink> makeFriend(@RequestBody @Valid FriendLink friend) {
		var host = URI.create(friend.url).getHost();
		friend.createTime = clock.instant();

		if (friendMap.put(host, friend) != null) {
			throw new ResourceStateException("指定站点的友链已存在");
		}
		hostList.add(host);
		validateMap.put(host, new ValidateRecord(friend.url, friend.friendPage, friend.createTime));

		return ResponseEntity.created(URI.create("/friends/" + host)).body(friend);
	}

	// 仅用于排序，假定输入的友链都是已存在的
	@RequireAuthorize
	@PutMapping
	public void updateAll(@RequestBody List<String> hostList) {
		this.hostList.clear();
		this.hostList.addAll(hostList);
	}

	@RequireAuthorize
	@DeleteMapping("/{host}")
	public ResponseEntity<Void> rupture(@PathVariable String host) {
		friendMap.remove(host);
		validateMap.remove(host);
		hostList.remove(host);

		return ResponseEntity.noContent().build();
	}
}
