package net.kaciras.blog.controller;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.user.BanRecord;
import net.kaciras.blog.domain.user.User;
import net.kaciras.blog.domain.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
final class UserController {

	private final UserService userService;

	@GetMapping("/{id}")
	public User get(@PathVariable int id) {
		return userService.getUser(id);
	}

	@PostMapping("/{id}/banRecords")
	public ResponseEntity setAvailability(@PathVariable int id,
										  @RequestParam(required = false, defaultValue = "0") long time,
										  @RequestParam String cause) throws URISyntaxException {
		int bid = userService.ban(id, time, cause);
		String location = String.format("/users/%d/banRecords/%d", id, bid);
		return ResponseEntity.created(new URI(location)).build();
	}

	@PostMapping("/{id}/banRecords/{bid}/undoRecord")
	public ResponseEntity unban(@PathVariable int id, @PathVariable int bid,
								@RequestParam String cause) throws URISyntaxException {
		userService.unban(id, bid, cause);
		String location = String.format("/users/%d/banRecords/%d/undoRecord", id, bid);
		return ResponseEntity.created(new URI(location)).build();
	}

	@GetMapping("/{id}/banRecords")
	public List<BanRecord> getBanRecords(@PathVariable int id) {
		return userService.getBanRedords(id);
	}

}
