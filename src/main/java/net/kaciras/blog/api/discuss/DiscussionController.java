package net.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.exception.ResourceStateException;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/discussions")
class DiscussionController {

	private final DiscussionService discussionService;

	private final DiscussMapper mapper;

	@GetMapping
	public Map<String, ?> getList(DiscussionQuery query, Pageable pageable) {
		query.setPageable(pageable);

		var size = discussionService.count(query);
		if (query.isMetaonly()) {
			return Map.of("total", size);
		}

		var result = mapper.toDiscussionView(discussionService.getList(query));
		return Map.of("total", size, "items", result);
	}

	@PostMapping
	public ResponseEntity post(@RequestBody AddRequest request) {
		var id = discussionService.add(request.getObjectId(), request.getType(), request.getContent());
		return ResponseEntity.created(URI.create("/discussions/" + id)).build();
	}

	@RequireAuthorize
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable long id) {
		discussionService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@RequireAuthorize
	@PostMapping("/{id}/restoration")
	public ResponseEntity postRestoration(@PathVariable int id) {
		discussionService.restore(id);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 查询指定评论的回复（楼中楼）。
	 *
	 * 楼中楼的URL主要使用子资源的形式，虽然getList()方法通过设置请求参数
	 * 也能做到相同的功能，但使用子资源更加语义化。
	 *
	 * @param id 评论ID
	 * @param pageable 分页参数
	 * @return 回复列表
	 */
	@GetMapping("/{id}/replies")
	public List<DiscussionVo> getReplies(@PathVariable long id, Pageable pageable) {
		var query = DiscussionQuery.byParent(id);
		query.setPageable(pageable);
		return mapper.toReplyView(discussionService.getList(query));
	}

	@PostMapping("/{id}/replies")
	public ResponseEntity<Void> addReply(@PathVariable long id, @RequestBody String content) {
		var newId = discussionService.addReply(id, content);
		return ResponseEntity.created(URI.create("/discussions/" + newId)).build();
	}

	/**
	 * 点赞功能，每个用户对每个评论只能点赞一次，若重复点赞则返回409.
	 *
	 * @param id 要点赞的评论ID
	 * @return 响应
	 */
	@PostMapping("/{id}/votes")
	public ResponseEntity<Void> postVote(@PathVariable int id) {
		SecurityContext.requireLogin();
		discussionService.voteUp(id);
		return ResponseEntity.created(URI.create("discussions/" + id + "/votes")).build();
	}

	@DeleteMapping("/{id}/votes")
	public ResponseEntity<Void> revokeVote(@PathVariable int id) {
		SecurityContext.requireLogin();
		discussionService.revokeVote(id);
		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler(ResourceStateException.class)
	public ResponseEntity<Void> handleException() {
		return ResponseEntity.status(409).build();
	}
}
