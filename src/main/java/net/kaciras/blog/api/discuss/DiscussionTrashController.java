package net.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/trash/discussions")
final class DiscussionTrashController {

	private final DiscussionService discussionService;

	@PostMapping("/{id}/restoration")
	public ResponseEntity postRestoration(@PathVariable int id) {
		discussionService.restore(id);
		return ResponseEntity.noContent().build();
	}
}
