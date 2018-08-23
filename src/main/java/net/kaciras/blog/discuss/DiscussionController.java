package net.kaciras.blog.discuss;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import net.kaciras.blog.infrastructure.exception.ResourceStateException;
import net.kaciras.blog.infrastructure.text.TextUtil;
import net.kaciras.blog.user.UserService;
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
	private final VoMapper mapper;

	@GetMapping
	public Map<String, ?> getList(@Valid DiscussionQuery query) {
		int size = discussionService.count(query);
		if (query.isMetaonly()) {
			return Map.of("total", size);
		}
		var ds = discussionService.getList(query);
		var result = new ArrayList<DiscussionVo>(ds.size());

		for (Discussion d : ds) {
			DiscussionVo v = mapper.discussionView(d);
			v.setUser(userService.getUser(d.getUserId()));
			result.add(v);
		}
		return Map.of("total", size, "list", result);
	}

	@PostMapping
	public ResponseEntity post(@RequestBody Discussion to) {
		if(TextUtil.isDanger(TextUtil.toSimplified(to.getContent()))) {
			throw new RequestArgumentException("评论中存在敏感词");
		}
		int id = discussionService.add(to);
		return ResponseEntity.created(URI.create("/discussions/" + id)).build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable int id) {
		discussionService.delete(id);
		return ResponseEntity.noContent().build();
	}

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
