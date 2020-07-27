package com.kaciras.blog.api.friend;

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
		var host = repository.addFriend(friend);
		validateService.addForValidate(host, friend);
		return ResponseEntity.created(URI.create("/friends/" + host)).body(friend);
	}

	@RequireAuthorize
	@DeleteMapping("/{host}")
	public void rupture(@PathVariable String host) {
		repository.remove(host);
		validateService.removeFromValidate(host);
	}

	@RequireAuthorize
	@PutMapping
	public void updateSort(@RequestBody String[] hostList) {
		repository.updateSort(hostList);
	}
}
