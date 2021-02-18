package com.kaciras.blog.api.discuss;

import lombok.Cleanup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 执行列表查询请求的类，负责查询评论列表，并将所需要的父评论和下级评论也加入到结果中。
 * <p>
 * 因为要返回 ID 与对象分离的结果，比起用每次查询都要传参 HashMap，
 * 还是把 objects 保存为字段更舒服一些。
 * 另外这里的代码接近一百行，写在控制器里不好看，所以单独提取到一个类。
 */
@RequiredArgsConstructor
final class QueryWorker {

	@Getter
	private final Map<Integer, DiscussionVO> objects = new HashMap<>();

	private final DiscussionRepository repository;
	private final TopicRegistration topics;
	private final ViewModelMapper mapper;

	/**
	 * 用于收集评论的 ID 然后一次性从数据库里查出，解决 1 + N 问题。
	 */
	private Set<Integer> additional;

	/**
	 * 执行查询，返回评论的ID列表，用 {@code getObjects} 获取视图对象表。
	 *
	 * @param query 查询条件
	 * @return 评论的ID列表
	 */
	public List<Integer> execute(DiscussionQuery query) {
		@Cleanup var stream = findAll(query);

		if (query.isIncludeTopic()) {
			stream = stream.peek(v -> v.topic = topics.get(v.type, v.objectId));
		}

		if (query.isIncludeParent()) {
			additional = new HashSet<>();
			stream = stream
					.peek(this::addParentToAdditional)
					.onClose(this::attachAdditional);
		}

		if (query.getChildCount() > 0) {
			var page = PageRequest.of(0, query.getChildCount());
			stream = stream.peek(v -> attachChildren(v, page));
		}

		return collectId(stream);
	}

	/**
	 * 查询评论列表，将其中每个评论转换为视图对象并加入到 object 中。
	 * <p>
	 * 这个过程有多个地方使用所以就提取出来了。
	 *
	 * @param query 查询条件
	 * @return 视图对象的流，用于后续操作
	 */
	private Stream<DiscussionVO> findAll(DiscussionQuery query) {
		return repository.findAll(query)
				.stream()
				.map(mapper::toViewObject)
				.peek(v -> objects.put(v.id, v));
	}

	/**
	 * 引用模式，将每个结果的父评论加入到附加查询集合中。
	 */
	private void addParentToAdditional(DiscussionVO vo) {
		var id = vo.parent;
		if (id > 0 && !objects.containsKey(id)) {
			additional.add(id);
		}
	}

	/**
	 * 楼中楼模式，将每个结果的下级评论加入到 objects 中，并把它们的 ID 保存到 replies 字段。
	 */
	private void attachChildren(DiscussionVO vo, Pageable pageable) {
		var childrenQuery = new DiscussionQuery()
				.setNestId(vo.id)
				.setPageable(pageable);
		vo.replies = collectId(findAll(childrenQuery));
	}

	private List<Integer> collectId(Stream<DiscussionVO> stream) {
		return stream.map(vo -> vo.id).collect(Collectors.toList());
	}

	// 有点长写在 lambda 里不好看所以拿出来了。
	private void attachAdditional() {
		repository.get(additional).forEach(v -> objects.put(v.getId(), mapper.toViewObject(v)));
	}
}
