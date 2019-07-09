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
import java.util.Collections;

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

	/*
	 * 【知识点】Pageable.sort 的自动绑定：
	 * 请求中包含 sort=f0,f1,DESC 会解析为两个 Order，对应 f0, f1 两个字段，它们都是DESC降序。
	 * 如果需要混合升降顺序，则得使用多个 sort 参数：sort=f0,ASC&sort=f1,DESC
	 * Qualifier, SortDefault, SortDefaults 可以改变一些默认的行为，SpringBoot 也提供了对参数名的配置。
	 */
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
		return new ListQueryView<>(size, mapper.toAggregatedView(result, Utils.addressFromRequest(request)));
	}

	@PostMapping
	public ResponseEntity<DiscussionVo> post(HttpServletRequest request, @RequestBody PublishInput input) {
		var addr = Utils.addressFromRequest(request);
		var discussion = discussionService.add(input, addr);

		// HACK: 越来越觉得要尽快迁移GraphQL了
		var vo = mapper.toReplyView(discussion);
		if (discussion.getParent() == 0) {
			vo.setReplies(Collections.emptyList());
		}

		return ResponseEntity.created(URI.create("/discussions/" + discussion.getId())).body(vo);
	}

	@RequireAuthorize // 当前仅支持管理者更新评论
	@PatchMapping
	public ResponseEntity<Void> patch(@RequestBody PatchInput input) {
		discussionService.batchUpdate(input);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 点赞功能，每个用户对每个评论只能点赞一次，若重复点赞则返回409.
	 *
	 * @param id 要点赞的评论ID
	 */
	@PostMapping("/{id}/votes")
	public ResponseEntity<Void> postVote(@PathVariable int id, HttpServletRequest request) {
		discussionService.voteUp(id, Utils.addressFromRequest(request));
		return ResponseEntity.created(URI.create("discussions/" + id + "/votes")).build();
	}

	@DeleteMapping("/{id}/votes")
	public ResponseEntity<Void> revokeVote(@PathVariable int id, HttpServletRequest request) {
		discussionService.revokeVote(id, Utils.addressFromRequest(request));
		return ResponseEntity.noContent().build();
	}
}
