package net.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.user.UserService;
import net.kaciras.blog.infrastructure.exception.ResourceStateException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/discussions")
final class DiscussionController {

	private final DiscussionService discussionService;
	private final UserService userService;
	private final DiscussMapper mapper;

	@GetMapping
	public Map<String, ?> getList(@Valid DiscussionQuery query) {
		var size = discussionService.count(query);
		if (query.isMetaonly()) {
			return Map.of("total", size);
		}
		var ds = discussionService.getList(query);
		var result = new ArrayList<DiscussionVo>(ds.size());

		for (var discussion : ds) {
			var vo = mapper.discussionView(discussion);
			vo.setUser(userService.getUser(discussion.getUserId()));
			result.add(vo);
		}
		return Map.of("total", size, "items", result);
	}

	@PostMapping
	public ResponseEntity post(@RequestBody AddDiscussionRequest request) {
		var id = discussionService.add(request.getObjectId(), request.getType(), request.getContent());
		return ResponseEntity.created(URI.create("/discussions/" + id)).build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable long id) {
		discussionService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/replies")
	public ResponseEntity<Void> addReply(@PathVariable long id, @RequestBody String content) {
		var newId = discussionService.addReply(id, content);
		return ResponseEntity.created(URI.create("/discussions/" + newId)).build();
	}

	/**
	 * 点赞功能
	 *
	 * @param id 要点赞的评论ID
	 * @return 响应
	 */
	@PostMapping("/{id}/vote")
	public ResponseEntity<Void> postVote(@PathVariable int id) {
		discussionService.voteUp(id);//409
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}/vote")
	public ResponseEntity<Void> revokeVote(@PathVariable int id) {
		discussionService.revokeVote(id);
		return ResponseEntity.noContent().build();
	}

	//重复点赞
	@ExceptionHandler(ResourceStateException.class)
	public ResponseEntity<Void> handleException(ResourceStateException ex) {
		return ResponseEntity.status(409).build();
	}
}
