package net.kaciras.blog.facade.controller;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.discuss.Discussion;
import net.kaciras.blog.domain.discuss.DiscussionQuery;
import net.kaciras.blog.domain.discuss.DiscussionService;
import net.kaciras.blog.domain.user.UserService;
import net.kaciras.blog.facade.pojo.DiscussionVO;
import net.kaciras.blog.facade.pojo.PojoMapper;
import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import net.kaciras.blog.infrastructure.exception.ResourceStateException;
import net.kaciras.blog.infrastructure.text.TextUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/discussions")
final class DiscussionController {

	private final DiscussionService discussionService;
	private final UserService userService;
	private final PojoMapper mapper;

	@GetMapping
	public Map<String, ?> getList(@Valid DiscussionQuery query) {
		int size = discussionService.count(query);
		if (query.isMetaonly()) {
			return Map.of("total", size);
		}
		var ds = discussionService.getList(query);
		var result = new ArrayList<DiscussionVO>(ds.size());

		for (Discussion d : ds) {
			DiscussionVO v = mapper.toDiscussionVO(d);
			v.setUser(mapper.toUserVo(userService.getUser(d.getUserId())));
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
