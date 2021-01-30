package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.MappingListView;
import com.kaciras.blog.api.Utils;
import com.kaciras.blog.api.config.BindConfig;
import com.kaciras.blog.api.notice.NoticeService;
import com.kaciras.blog.infra.exception.PermissionException;
import com.kaciras.blog.infra.exception.RequestArgumentException;
import com.kaciras.blog.infra.principal.RequirePermission;
import com.kaciras.blog.infra.principal.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;

/**
 * 评论的数据结构是一颗含有不同类型对象的树，根节点是主题，下面的节点是评论。
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/discussions")
class DiscussionController {

	private final TopicRegistration topics;
	private final DiscussionRepository repository;
	private final ViewModelMapper mapper;

	private final NoticeService noticeService;

	@BindConfig("discussion")
	@Setter
	private DiscussionOptions options;

	/**
	 * 验证查询参数是否合法，该方法只检查用户的请求，对于内部查询不限制。
	 * 查询必须包含的过滤条件，如果是管理则可以无视。
	 *
	 * @param query 查询对象
	 */
	private void verifyQuery(DiscussionQuery query) {
		if (query.getNestId() == null && (query.getType() == null || query.getObjectId() == null)) {
			SecurityContext.require("POWER_QUERY");
		}
		if (query.getState() != DiscussionState.Visible) {
			SecurityContext.require("POWER_QUERY");
		}
		if (query.getPageable() == null) {
			query.setPageable(PageRequest.of(0, 30));
		} else if (query.getPageable().getPageSize() > 30) {
			throw new RequestArgumentException("查询的数量过多");
		}
	}

	/*
	 * 【Pageable.sort 的自动绑定】
	 * 请求中包含 sort=f0,f1,DESC 会解析为两个 Order，对应 f0, f1 两个字段，它们都是DESC降序。
	 * 如果需要混合升降顺序，则得使用多个 sort 参数：sort=f0,ASC&sort=f1,DESC
	 * Qualifier, SortDefault, SortDefaults 可以改变一些默认的行为，SpringBoot 也提供了对参数名的配置。
	 */
	@GetMapping
	public MappingListView<Integer, DiscussionVo> getList(@Valid DiscussionQuery query, Pageable pageable) {
		query.setPageable(pageable);
		verifyQuery(query);

		var session = new QueryCacheSession(repository, mapper);
		var items = session.execute(query);
		var total = repository.count(query);

		return new MappingListView<>(total, items, session.getObjects());
	}

	@PostMapping
	public ResponseEntity<DiscussionVo> post(
			HttpServletRequest request,
			@Valid @RequestBody PublishInput input) {
		if (options.disabled) {
			throw new PermissionException("已禁止评论");
		}
		if (options.loginRequired) {
			SecurityContext.requireLogin();
		}

		var discussion = mapper.fromInput(input);
		discussion.setUserId(SecurityContext.getUserId());
		discussion.setState(options.moderation
				? DiscussionState.Moderation
				: DiscussionState.Visible);
		discussion.setAddress(Utils.addressFromRequest(request));

		//
		Topic topic;
		if (discussion.getParent() == 0) {
			topic = topics.get(discussion);
			repository.add(discussion);
		} else {
			repository.add(discussion);
			topic = topics.get(discussion);
		}

		// 发送通知提醒，自己的评论就不用了
		if (discussion.getUserId() != 2) {
			noticeService.notify(mapper.toActivity(discussion, topic));
		}

		return ResponseEntity
				.created(URI.create("/discussions/" + discussion.getId()))
				.body(mapper.toViewObject(discussion));
	}

	/**
	 * 批量更新评论的状态，仅博主能使用。
	 */
	@RequirePermission
	@PatchMapping
	@Transactional
	public ResponseEntity<Void> patch(@RequestBody PatchInput input) {
		for (var id : input.ids) {
			repository.updateState(id, input.state);
		}
		return ResponseEntity.noContent().build();
	}
}
