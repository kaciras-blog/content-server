package net.kaciras.blog.facade.controller;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.discuss.DiscussionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/trash/discussions")
public final class DiscussionTrashController {

	private final DiscussionService discussionService;

	@PostMapping("/{id}/restoration")
	public ResponseEntity postRestoration(@PathVariable int id) {
		discussionService.restore(id);
		return ResponseEntity.noContent().build();
	}
}
