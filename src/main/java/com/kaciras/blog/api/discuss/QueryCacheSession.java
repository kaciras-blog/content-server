package com.kaciras.blog.api.discuss;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
final class QueryCacheSession {

	@Getter
	private final Map<Integer, DiscussionVo> objects = new HashMap<>();

	private final DiscussionRepository repository;
	private final ViewModelMapper mapper;

	private void attachChildren(DiscussionVo viewObject, DiscussionQuery query) {
		var childrenQuery = new DiscussionQuery()
				.setParent(viewObject.getId())
				.setPageable(PageRequest.of(0, query.getReplySize()));

		viewObject.setReplies(findAll(childrenQuery));
	}

	List<Integer> findAllWithChildren(DiscussionQuery query) {
		var discussions = repository.findAll(query);

		discussions.stream().map(mapper::toViewObject).forEach(v -> objects.put(v.getId(), v));

		discussions.stream().map(mapper::toViewObject)
				.peek(v -> attachChildren(v, query))
				.forEach(v -> objects.put(v.getId(), v));

		return discussions.stream().map(Discussion::getId).collect(Collectors.toList());
	}

	List<Integer> findAll(DiscussionQuery query) {
		var discussions = repository.findAll(query);

		discussions.stream().map(mapper::toViewObject).forEach(v -> objects.put(v.getId(), v));

		discussions.stream().map(Discussion::getParent)
				.dropWhile(objects::containsKey)
				.map(id -> repository.get(id).orElseThrow())
				.map(mapper::toViewObject)
				.forEach(v -> objects.put(v.getId(), v));

		return discussions.stream().map(Discussion::getId).collect(Collectors.toList());
	}
}
