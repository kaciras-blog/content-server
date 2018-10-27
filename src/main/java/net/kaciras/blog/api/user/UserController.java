package net.kaciras.blog.api.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Duration;
import java.util.List;

@RequireAuthorize // 仅管理员
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
class UserController {

	private final UserRepository repository;
	private final UserManager userManager;

	@GetMapping("/{id}")
	public UserVo get(@PathVariable int id) {
		return userManager.getUser(id);
	}

	@PostMapping("/{id}/ban-records")
	public ResponseEntity ban(@PathVariable int id,
							  @RequestParam(required = false, defaultValue = "0") long time,
							  @RequestParam String cause) {
		var bid = repository.get(id)
				.ban(SecurityContext.getUserId(), Duration.ofSeconds(time), cause);

		var location = String.format("/users/%d/banRecords/%d", id, bid);
		return ResponseEntity.created(URI.create(location)).build();
	}

	@PostMapping("/{id}/banRecords/{bid}/undoRecord")
	public ResponseEntity unban(@PathVariable int id,
								@PathVariable int bid,
								@RequestParam String cause) {
		repository.get(id)
				.unBan(bid, SecurityContext.getUserId(), cause);

		var location = String.format("/users/%d/banRecords/%d/undoRecord", id, bid);
		return ResponseEntity.created(URI.create(location)).build();
	}

	@GetMapping("/{id}/banRecords")
	public List<BanRecord> getBanRecords(@PathVariable int id) {
		return repository.get(id).getBanRecords();
	}

	@PatchMapping("/{id}")
	public ResponseEntity<Void> patch(@PathVariable int id, @RequestBody PatchMap patchMap) {
		var user = repository.get(id);

		if(patchMap.getHead() != null) {
			user.updateHead(patchMap.getHead());
		}
		return ResponseEntity.noContent().build();
	}
}
