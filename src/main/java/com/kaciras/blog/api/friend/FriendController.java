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
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.List;

/**
 * 【一致性的】
 * 假定博主不会在同一时间做多个操作，这样就只考虑检测任务的并发。
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/friends")
class FriendController {

	private final RedisList<FriendLink> friendList;
	private final RedisMap<String, ValidateRecord> validateRecords;

	private final Clock clock;

	@GetMapping
	public List<FriendLink> getFriends() {
		return friendList;
	}

	// 未加事务
	@RequireAuthorize
	@PostMapping
	public ResponseEntity<FriendLink> makeFriend(@RequestBody @Valid FriendLink input) {
		var now = clock.instant();
		input.createTime = now;

		var host = URI.create(input.url).getHost();
		if (validateRecords.containsKey(host)) {
			throw new ResourceStateException("指定站点的友链已存在");
		}

		validateRecords.put(host, new ValidateRecord(input.url, input.friendPage, now));
		friendList.add(input);

		return ResponseEntity.created(URI.create("/friends/" + host)).body(input);
	}

	// 仅用于排序，假定输入的友链都是已存在的
	@RequireAuthorize
	@PutMapping
	public void updateAll(@RequestBody List<FriendLink> input) {
		friendList.clear();
		friendList.addAll(input);
	}

	@RequireAuthorize
	@DeleteMapping("/{host}")
	public ResponseEntity<Void> rupture(@PathVariable String host) throws URISyntaxException {
		var index = 0;

		for (var friend : friendList) {
			var h = new URI(friend.url).getHost();
			if (h.equals(host)) break;
			index++;
		}

		if (index >= friendList.size()) {
			return ResponseEntity.notFound().build();
		}

		friendList.remove(index);
		validateRecords.remove(host);

		return ResponseEntity.noContent().build();
	}
}
