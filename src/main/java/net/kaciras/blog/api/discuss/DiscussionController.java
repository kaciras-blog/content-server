package net.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.ListQueryView;
import net.kaciras.blog.api.Utils;
import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import net.kaciras.blog.infrastructure.exception.ResourceStateException;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/discussions")
class DiscussionController {

	private final DiscussRepository repository;
	private final DiscussionService discussionService;

	private final DiscussMapper mapper;

	/**
	 * 验证查询参数是否合法，该方法只检查用户的请求，对于内部查询不限制。
	 *
	 * @param query 查询对象
	 */
	private void verifyQuery(DiscussionQuery query) {
		if (query.getObjectId() == null && query.getUserId() == null && query.getState() == null) {
			throw new RequestArgumentException();
		}
		if (query.getState() != DiscussionState.Visible) {
			SecurityContext.require("POWER_QUERY");
		}
		if (query.getPageable().getPageSize() > 20) {
			throw new RequestArgumentException("查询的数量过多");
		}
	}

	@GetMapping
	public ListQueryView<DiscussionVo> getList(DiscussionQuery query, Pageable pageable) {
		query.setPageable(pageable);
		verifyQuery(query);

		var size = discussionService.count(query);
		var result = mapper.toDiscussionView(discussionService.getList(query));
		return new ListQueryView<>(size, result);
	}

	@PostMapping
	public ResponseEntity post(HttpServletRequest request, @RequestBody AddRequest message) {
		var addr = Utils.AddressFromRequest(request);
		var id = discussionService.add(message.getObjectId(), message.getType(), message.getContent(), addr);
		return ResponseEntity.created(URI.create("/discussions/" + id)).build();
	}

	@RequireAuthorize
	@PatchMapping("/{id}")
	public ResponseEntity<Void> patch(@PathVariable long id, @RequestBody PatchMap patchMap) {
		if (patchMap.state != null) {
			repository.get(id).updateState(patchMap.state);
		}
		return ResponseEntity.noContent().build();
	}

	/**
	 * 查询指定评论的回复（楼中楼）。
	 * <p>
	 * 楼中楼的URL主要使用子资源的形式，虽然getList()方法通过设置请求参数
	 * 也能做到相同的功能，但使用子资源更加语义化。
	 *
	 * @param id       评论ID
	 * @param pageable 分页参数
	 * @return 回复列表
	 */
	@GetMapping("/{id}/replies")
	public ListQueryView<DiscussionVo> getReplies(@PathVariable int id, Pageable pageable) {
		var replies = repository.get(id).getReplyList();
		var query = new DiscussionQuery().setObjectId(id).setPageable(pageable);
		return new ListQueryView<>(replies.size(), mapper.toReplyView(replies.select(query)));
	}

	@PostMapping("/{id}/replies")
	public ResponseEntity<Void> addReply(HttpServletRequest request, @PathVariable long id, @RequestBody String content) {
		var addr = Utils.AddressFromRequest(request);
		var newId = discussionService.addReply(id, content, addr);
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
		repository.get(id).getVoterList().add(SecurityContext.getUserId());

		return ResponseEntity.created(URI.create("discussions/" + id + "/votes")).build();
	}

	@DeleteMapping("/{id}/votes")
	public ResponseEntity<Void> revokeVote(@PathVariable int id) {
		SecurityContext.requireLogin();
		repository.get(id).getVoterList().remove(SecurityContext.getUserId());

		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler(ResourceStateException.class)
	public ResponseEntity<Void> handleException() {
		return ResponseEntity.status(409).build();
	}
}
