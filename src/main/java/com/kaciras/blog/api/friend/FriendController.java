package com.kaciras.blog.api.friend;

import com.kaciras.blog.infra.exception.ResourceNotFoundException;
import com.kaciras.blog.infra.exception.ResourceStateException;
import com.kaciras.blog.infra.principal.RequireAuthorize;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

/**
 * 【一致性的讨论】
 * 假定博主不会在同一时间做多个修改操作，这样就只考虑检测任务的并发。
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/friends")
class FriendController {

	private final FriendValidateService validateService;

	private final FriendRepository repository;

	@GetMapping
	public FriendLink[] getFriends() {
		return repository.getFriends();
	}

	@RequireAuthorize
	@PostMapping
	public ResponseEntity<FriendLink> makeFriend(@RequestBody @Valid FriendLink friend) {
		if (!repository.addFriend(friend)) {
			throw new ResourceStateException("指定站点的友链已存在");
		}
		validateService.addForValidate(friend);
		return ResponseEntity.created(URI.create("/friends/" + friend.url.getHost())).body(friend);
	}

	@RequireAuthorize
	@DeleteMapping("/{host}")
	public void rupture(@PathVariable String host) {
		if (!repository.remove(host)) {
			throw new ResourceNotFoundException();
		}
		validateService.removeFromValidate(host);
	}

	@RequireAuthorize
	@PutMapping
	public void updateSort(@RequestBody String[] hostList) {
		repository.updateSort(hostList);
	}
}
