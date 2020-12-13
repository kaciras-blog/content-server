package com.kaciras.blog.api.discuss;

import com.kaciras.blog.infra.exception.RequestArgumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
class DiscussionRepositoryTest {

	@Autowired
	private DiscussionRepository repository;

	private static Discussion newDiscussion() {
		var value = new Discussion();
		value.setState(DiscussionState.Visible);
		value.setContent("test content");
		value.setAddress(InetAddress.getLoopbackAddress());
		return value;
	}

	private Discussion addData(int parent) {
		return addData(parent, DiscussionState.Visible);
	}

	private Discussion addData(int parent, DiscussionState state) {
		var value = newDiscussion();
		value.setParent(parent);
		value.setState(state);
		repository.add(value);
		return value;
	}

	private static Stream<Arguments> invalidDiscussions() {
		var noContent = newDiscussion();
		noContent.setContent(null);

		var noAddress = newDiscussion();
		noAddress.setAddress(null);

		var noState = newDiscussion();
		noState.setState(null);

		return Stream.of(Arguments.of(noContent), Arguments.of(noAddress), Arguments.of(noState));
	}

	@MethodSource("invalidDiscussions")
	@ParameterizedTest
	void addInvalid(Discussion value) {
		assertThatThrownBy(() -> repository.add(value)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void add() {
		var value = newDiscussion();
		repository.add(value);

		assertThat(value.getFloor()).isEqualTo(1);
		assertThat(value.getTime()).isNotNull();

		// Mariadb 的事务不回滚自增值，只能用大于来判断
		assertThat(value.getId()).isGreaterThanOrEqualTo(1);
	}

	@Test
	void findWithInvalidSort() {
		var query = new DiscussionQuery();
		query.setPageable(PageRequest.of(0, 1, Sort.by("content")));

		assertThatThrownBy(() -> repository.findAll(query)).isInstanceOf(RequestArgumentException.class);
	}

	@Test
	void findAll() {
		var _1 = addData(0);
		addData(_1.getId());
		addData(_1.getId(), DiscussionState.Deleted);

		var query = new DiscussionQuery();
		query.setParent(0);
		var list = repository.findAll(query);

		assertThat(list).hasSize(1);
		assertThat(list.get(0).getReplyCount()).isEqualTo(1);
	}

	@Test
	void updateAllToDeleted() {
		var _1 = addData(0);
		var _2 = addData(_1.getId());

		repository.updateAll(List.of(_1.getId(), _2.getId()), DiscussionState.Deleted);

		var query = new DiscussionQuery();
		query.setState(DiscussionState.Deleted);
		var list = repository.findAll(query);

		assertThat(list).hasSize(2);
		assertThat(list.get(0).getState()).isEqualTo(DiscussionState.Deleted);
		assertThat(list.get(0).getReplyCount()).isEqualTo(0);
		assertThat(list.get(1).getState()).isEqualTo(DiscussionState.Deleted);
	}

	@Test
	void updateAllToVisible() {
		var _1 = addData(0, DiscussionState.Moderation);
		var _2 = addData(_1.getId(), DiscussionState.Moderation);

		repository.updateAll(List.of(_1.getId(), _2.getId()), DiscussionState.Visible);

		var query = new DiscussionQuery();
		query.setState(DiscussionState.Visible);
		var list = repository.findAll(query);

		assertThat(list).hasSize(2);
		assertThat(list.get(0).getState()).isEqualTo(DiscussionState.Visible);
		assertThat(list.get(0).getReplyCount()).isEqualTo(1);
		assertThat(list.get(1).getState()).isEqualTo(DiscussionState.Visible);
	}

	@Test
	void getNonExists() {
		assertThat(repository.get(0)).isEmpty();
	}

	@Test
	void get() {
		var expected = newDiscussion();
		expected.setType(3);
		expected.setObjectId(7);
		repository.add(expected);

		var returnValue = repository.get(expected.getId());
		assertThat(returnValue).isPresent();

		var value = returnValue.get();
		assertThat(value.getType()).isEqualTo(3);
		assertThat(value.getObjectId()).isEqualTo(7);
		assertThat(value.getState()).isEqualTo(DiscussionState.Visible);
		assertThat(value.getTime()).isNotNull();
		assertThat(value.getFloor()).isEqualTo(1);
		assertThat(value.getContent()).isEqualTo(expected.getContent());
		assertThat(value.getAddress()).isNotNull();
	}
}
