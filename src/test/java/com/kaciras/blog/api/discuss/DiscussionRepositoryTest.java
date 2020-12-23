package com.kaciras.blog.api.discuss;

import com.kaciras.blog.infra.exception.RequestArgumentException;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.BeforeEach;
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
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
class DiscussionRepositoryTest {

	@Autowired
	private DiscussionRepository repository;

	@Autowired
	private SqlSession sqlSession;

	/**
	 * Mariadb 和 MySQL 的事务不能回滚自增值，导致新加的行 ID 无法预测，必须手动重置下。
	 */
	@BeforeEach
	void resetAutoIncrement() throws SQLException {
		sqlSession.getConnection()
				.createStatement()
				.execute("ALTER TABLE discussion AUTO_INCREMENT = 1");
	}

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
		var top0 = addData(0);
		var reply0 = addData(top0.getId());
		var top1 = addData(0);
		var reply1 = addData(top0.getId());

		// Mariadb 的事务不回滚自增值，只能用大于来判断
		assertThat(top0.getId()).isGreaterThanOrEqualTo(1);
		assertThat(top0.getTime()).isNotNull();
		assertThat(top0.getReplyFloor()).isEqualTo(1);

		assertThat(top0.getTopicFloor()).isEqualTo(1);
		assertThat(reply0.getTopicFloor()).isEqualTo(2);
		assertThat(top1.getTopicFloor()).isEqualTo(3);
		assertThat(reply1.getTopicFloor()).isEqualTo(4);

		assertThat(top0.getReplyFloor()).isEqualTo(1);
		assertThat(reply0.getReplyFloor()).isEqualTo(1);
		assertThat(top1.getReplyFloor()).isEqualTo(2);
		assertThat(reply1.getReplyFloor()).isEqualTo(2);
	}

	@Test
	void findWithInvalidSort() {
		var query = new DiscussionQuery();
		query.setPageable(PageRequest.of(0, 1, Sort.by("content")));

		assertThatThrownBy(() -> repository.findAll(query)).isInstanceOf(RequestArgumentException.class);
	}

	private static Stream<Arguments> queries() {
		return Stream.of(
				Arguments.of(new DiscussionQuery().setParent(0))
		);
	}

	@MethodSource("queries")
	@ParameterizedTest
	void findAll(DiscussionQuery query) {
		var _1 = addData(0);
		addData(_1.getId());
		addData(_1.getId(), DiscussionState.Deleted);

		var list = repository.findAll(query);

		assertThat(list).hasSize(1);
		assertThat(list.get(0).getReplyCount()).isEqualTo(1);
	}

	@Test
	void updateStateNonExists() {
		assertThatThrownBy(() -> repository.updateState(777, DiscussionState.Deleted))
				.isInstanceOf(RequestArgumentException.class);
	}

	private static Stream<Arguments> stateChanges() {
		return Stream.of(
				Arguments.of(DiscussionState.Visible, DiscussionState.Visible, 1),
				Arguments.of(DiscussionState.Visible, DiscussionState.Deleted, 0),
				Arguments.of(DiscussionState.Deleted, DiscussionState.Deleted, 0),
				Arguments.of(DiscussionState.Deleted, DiscussionState.Visible, 1),
				Arguments.of(DiscussionState.Moderation, DiscussionState.Visible, 1),
				Arguments.of(DiscussionState.Moderation, DiscussionState.Deleted, 0)
		);
	}

	@MethodSource("stateChanges")
	@ParameterizedTest
	void updateState(DiscussionState old, DiscussionState neW, int replyCount) {
		var _1 = addData(0);
		var _2 = addData(_1.getId(), old);

		repository.updateState(_2.getId(), neW);

		var discussion = repository.get(_2.getId()).orElseThrow();
		assertThat(discussion.getState()).isEqualTo(neW);

		var parent = repository.get(_1.getId()).orElseThrow();
		assertThat(parent.getReplyCount()).isEqualTo(replyCount);
	}

	@Test
	void updateStateToVisible() {
		var _1 = addData(0);
		var _2 = addData(_1.getId(), DiscussionState.Moderation);

		repository.updateState(_2.getId(), DiscussionState.Visible);

		var discussion = repository.get(_2.getId());
		assertThat(discussion).isPresent();
		assertThat(discussion.get().getState()).isEqualTo(DiscussionState.Visible);

		var parent = repository.get(_1.getId());
		assertThat(parent).isPresent();
		assertThat(parent.get().getReplyCount()).isEqualTo(1);
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
		assertThat(value.getTopicFloor()).isEqualTo(1);
		assertThat(value.getReplyFloor()).isEqualTo(1);
		assertThat(value.getContent()).isEqualTo(expected.getContent());
		assertThat(value.getAddress()).isNotNull();
	}
}
