package net.kaciras.blog.api.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RequireAuthorize
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
class UserController {

	private final UserService userService;

	@GetMapping("/{id}")
	public UserVo get(@PathVariable int id) {
		return userService.getUser(id);
	}

	@PostMapping("/{id}/ban-records")
	public ResponseEntity setAvailability(@PathVariable int id,
										  @RequestParam(required = false, defaultValue = "0") long time,
										  @RequestParam String cause) throws URISyntaxException {
		var bid = userService.ban(id, time, cause);
		var location = String.format("/users/%d/banRecords/%d", id, bid);
		return ResponseEntity.created(new URI(location)).build();
	}

	@PostMapping("/{id}/banRecords/{bid}/undoRecord")
	public ResponseEntity unban(@PathVariable int id, @PathVariable int bid,
								@RequestParam String cause) throws URISyntaxException {
		userService.unban(id, bid, cause);
		var location = String.format("/users/%d/banRecords/%d/undoRecord", id, bid);
		return ResponseEntity.created(new URI(location)).build();
	}

	@GetMapping("/{id}/banRecords")
	public List<BanRecord> getBanRecords(@PathVariable int id) {
		return userService.getBanRedords(id);
	}
}
