package com.kaciras.blog.api.friend;

import com.kaciras.blog.infra.exception.ResourceNotFoundException;
import com.kaciras.blog.infra.exception.ResourceStateException;
import com.kaciras.blog.infra.principal.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

/**
 * 修改相关的 API 都加了锁，保证不会有线程安全问题。
 * 这些修改 API 只有博主自己偶尔用用，所以不需要考虑性能。
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/friends")
class FriendController {

	private final FriendValidateService validateService;

	private final FriendRepository repository;

	/**
	 * 有朋友就要骄傲地展示出来!
	 *
	 * @return 友链列表
	 */
	@GetMapping
	public FriendLink[] getFriends() {
		return repository.getAll();
	}

	/**
	 * ララララララララ、Oh welcome to the ジャパリパーク🎵
	 * ララララララララララ、集まれ友達 🎶
	 * ララララララララ、Oh welcome to the ジャパリパーク🎵
	 * ララララララララララ、素敵な旅立ち、ようこそジャパリパーク 🎶
	 *
	 * @param friend 是新的浮莲子哦
	 */
	@RequirePermission
	@PostMapping
	public synchronized ResponseEntity<FriendLink> makeFriend(@RequestBody @Valid FriendLink friend) {
		if (!repository.add(friend)) {
			throw new ResourceStateException("该站点的友链已存在");
		}
		validateService.addForValidate(friend);
		return ResponseEntity.created(URI.create("/friends/" + friend.url.getHost())).body(friend);
	}

	/**
	 * 友尽啦，绝交啦，不过以后还是可能再做朋友哦。
	 *
	 * @param host 友链的域名
	 */
	@RequirePermission
	@DeleteMapping("/{host}")
	public synchronized void rupture(@PathVariable String host) {
		if (!repository.remove(host)) {
			throw new ResourceNotFoundException();
		}
		validateService.removeFromValidate(host);
	}

	/**
	 * 无论变成什么样子，你都还是我的朋友哦~
	 *
	 * @param host   旧域名
	 * @param friend 新的样子~
	 */
	@RequirePermission
	@PutMapping("/{host}")
	public synchronized void update(@PathVariable String host, @RequestBody @Valid FriendLink friend) {
		if (!repository.update(host, friend)) {
			throw new ResourceNotFoundException();
		}
		validateService.removeFromValidate(host);
		validateService.addForValidate(friend);
	}

	/**
	 * 虽然很多人会避免对友链排序，他们使用字母序、添加顺序等。
	 * 但是啊，喜欢的友链排难道不应该在前面吗。
	 *
	 * @param hostList 新的顺序，是域名的列表
	 */
	@RequirePermission
	@PutMapping
	public synchronized void updateSort(@RequestBody String[] hostList) {
		repository.updateSort(hostList);
	}
}
