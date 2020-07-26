package com.kaciras.blog.api.friend;

import com.kaciras.blog.infra.exception.ResourceStateException;
import com.kaciras.blog.infra.principal.RequireAuthorize;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.time.Clock;
import java.util.List;
import java.util.Map;

/**
 * 【一致性的】
 * 假定博主不会在同一时间做多个操作，这样就只考虑检测任务的并发。
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/friends")
class FriendController {

	private final RedisList<String> hostList;
	private final RedisMap<String, ValidateRecord> validateRecords;
	private final RedisMap<String, FriendLink> friendMap;

	private final Clock clock;

	@GetMapping
	public FriendLink[] getFriends() {
		return makeList(hostList);
	}

	private FriendLink[] makeList(List<String> hosts) {
		var localMap = Map.copyOf(friendMap);
		return hosts.stream().map(localMap::get).toArray(FriendLink[]::new);
	}

	// 未加事务
	@RequireAuthorize
	@PostMapping
	public ResponseEntity<FriendLink> makeFriend(@RequestBody @Valid FriendLink input) {
		var host = URI.create(input.url).getHost();
		input.createTime = clock.instant();

		if (friendMap.put(host, input) == null) {
			throw new ResourceStateException("指定站点的友链已存在");
		}
		hostList.add(host);
		validateRecords.put(host, new ValidateRecord(input.url, input.friendPage, input.createTime));

		return ResponseEntity.created(URI.create("/friends/" + host)).body(input);
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
		validateRecords.remove(host);
		hostList.remove(host);

		return ResponseEntity.noContent().build();
	}
}
