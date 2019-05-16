package net.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.ListQueryView;
import net.kaciras.blog.api.Utils;
import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/discussions")
class DiscussionController {

	private final DiscussionService discussionService;
	private final ViewModelMapper mapper;

	/**
	 * 验证查询参数是否合法，该方法只检查用户的请求，对于内部查询不限制。
	 * 查询至少包含对象ID、用户ID、评论ID其中之一的过滤条件，如果是管理则可以无视。
	 *
	 * @param query 查询对象
	 */
	private void verifyQuery(DiscussionQuery query) {
		if (query.getObjectId() == null && query.getUserId() == null && query.getParent() == null) {
			SecurityContext.require("POWER_QUERY");
		}
		if (query.getState() != DiscussionState.Visible) {
			SecurityContext.require("POWER_QUERY");
		}
		if (query.getPageable() == null) {
			query.setPageable(PageRequest.of(0, 20));
		} else if (query.getPageable().getPageSize() > 20) {
			throw new RequestArgumentException("查询的数量过多");
		}
	}

	@GetMapping
	public ListQueryView<DiscussionVo> getList(HttpServletRequest request, DiscussionQuery query, Pageable pageable) {
		query.setPageable(pageable);
		verifyQuery(query);

		var size = discussionService.count(query);
		var result = discussionService.getList(query);

		if (query.isLinked()) {
			return new ListQueryView<>(size, mapper.toLinkedView(result));
		}
		if (query.getParent() != 0) {
			return new ListQueryView<>(size, mapper.toReplyView(result));
		}
		return new ListQueryView<>(size, mapper.toAggregatedView(result, Utils.AddressFromRequest(request)));
	}

	@PostMapping
	public ResponseEntity post(HttpServletRequest request, @RequestBody AddRequest message) {
		var addr = Utils.AddressFromRequest(request);
		var id = discussionService.add(message, addr);
		return ResponseEntity.created(URI.create("/discussions/" + id)).build();
	}

	@RequireAuthorize // 当前仅支持管理者更新评论
	@PatchMapping
	public ResponseEntity<Void> patch(@RequestBody PatchMap patchMap) {
		discussionService.batchUpdate(patchMap);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 点赞功能，每个用户对每个评论只能点赞一次，若重复点赞则返回409.
	 *
	 * @param id 要点赞的评论ID
	 */
	@PostMapping("/{id}/votes")
	public ResponseEntity<Void> postVote(@PathVariable int id, HttpServletRequest request) {
		discussionService.voteUp(id, Utils.AddressFromRequest(request));
		return ResponseEntity.created(URI.create("discussions/" + id + "/votes")).build();
	}

	@DeleteMapping("/{id}/votes")
	public ResponseEntity<Void> revokeVote(@PathVariable int id, HttpServletRequest request) {
		discussionService.revokeVote(id, Utils.AddressFromRequest(request));
		return ResponseEntity.noContent().build();
	}
}
