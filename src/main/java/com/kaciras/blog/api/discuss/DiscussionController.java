package com.kaciras.blog.api.discuss;

import com.kaciras.blog.api.ListQueryView;
import com.kaciras.blog.api.Utils;
import com.kaciras.blog.api.config.BindConfig;
import com.kaciras.blog.api.notification.NotificationService;
import com.kaciras.blog.infra.exception.PermissionException;
import com.kaciras.blog.infra.exception.RequestArgumentException;
import com.kaciras.blog.infra.principal.RequirePermission;
import com.kaciras.blog.infra.principal.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/discussions")
class DiscussionController {

	private final ChannelRegistration channels;
	private final DiscussionRepository repository;
	private final ViewModelMapper mapper;

	private final NotificationService notificationService;

	@SuppressWarnings("unused")
	@BindConfig("discussion")
	private DiscussionOptions options;

	/**
	 * 验证查询参数是否合法，该方法只检查用户的请求，对于内部查询不限制。
	 * 查询至少包含对象、用户、父亲论其中之一的过滤条件，如果是管理则可以无视。
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
	 * 【Pageable.sort 的自动绑定】
	 * 请求中包含 sort=f0,f1,DESC 会解析为两个 Order，对应 f0, f1 两个字段，它们都是DESC降序。
	 * 如果需要混合升降顺序，则得使用多个 sort 参数：sort=f0,ASC&sort=f1,DESC
	 * Qualifier, SortDefault, SortDefaults 可以改变一些默认的行为，SpringBoot 也提供了对参数名的配置。
	 */
	@GetMapping
	public ListQueryView<DiscussionVo> getList(DiscussionQuery query, Pageable pageable) {
		query.setPageable(pageable);
		verifyQuery(query);

		var discussions = repository.findAll(query);
		var size = repository.count(query);

		return new ListQueryView<>(size, mapper.toViewObject(discussions, query));
	}

	// 无论是否审核都返回视图，前端可以通过 state 判断
	@PostMapping
	public ResponseEntity<DiscussionVo> post(HttpServletRequest request, @Valid @RequestBody PublishInput input) {
		if (!options.isEnabled()) {
			throw new PermissionException("已禁止评论");
		}
		if (!options.isAllowAnonymous()) {
			SecurityContext.requireLogin();
		}

		var discussion = mapper.fromInput(input);
		discussion.setUserId(SecurityContext.getUserId());
		discussion.setState(options.isModeration() ? DiscussionState.Moderation : DiscussionState.Visible);
		discussion.setAddress(Utils.addressFromRequest(request));

		Discussion parent = null;

		if (discussion.getParent() != 0) {
			parent = repository.get(input.getParent()).orElseThrow(RequestArgumentException::new);
			discussion.setType(parent.getType());
			discussion.setObjectId(parent.getObjectId());
		}

		// 获取频道，同时检查其是否存在
		var channel = channels.getChannel(discussion);
		repository.add(discussion);

		if (discussion.getState() == DiscussionState.Visible) {
			notificationService.reportDiscussion(discussion, parent, channel);
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
