package net.kaciras.blog.domain;

import net.kaciras.blog.api.DeletedState;
import net.kaciras.blog.api.SecurtyContext;
import net.kaciras.blog.api.discuss.Discussion;
import net.kaciras.blog.api.discuss.DiscussionQuery;
import net.kaciras.blog.api.discuss.DiscussionService;
import net.kaciras.blog.infrastructure.exception.ResourceStateException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

class DiscussionServiceTest extends AbstractSpringTest {

	@Autowired
	private DiscussionService service;

	@BeforeEach
	void setUp() {
		SecurtyContext.setCurrentUser(1);
	}

	@Test
	void testGetOne() {
		Discussion one = service.getOne(7);
		Assertions.assertThat(one.getId()).isEqualTo(7);
		Assertions.assertThat(one.getFloor()).isEqualTo(5);
		Assertions.assertThat(one.getContent()).isEqualTo("楼中楼的二楼");
		Assertions.assertThat(one.getObjectId()).isEqualTo(1);
		Assertions.assertThat(one.getUserId()).isEqualTo(1);
		Assertions.assertThat(one.getVoteCount()).isEqualTo(2);
		Assertions.assertThat(one.isDeleted()).isFalse();
	}

	@Test
	void testAdd() {
		DiscussionQuery query = DiscussionQuery.byUser(1);
		Assertions.assertThat(service.count(query)).isEqualTo(2);

		Discussion discuz = new Discussion();
		discuz.setUserId(1);

		Assertions.assertThat(service.add(3, 0, "hellow world")).isGreaterThan(0);
		Assertions.assertThat(discuz.getFloor()).isEqualTo(1);
		Assertions.assertThat(service.count(query)).isEqualTo(3);

		//以下几个字段在插入时无效
		discuz.setDeleted(true);
		discuz.setFloor(9999);
		discuz.setVoteCount(8888);
		discuz.setId(7777);
		discuz.setTime(LocalDateTime.MIN);

		var id = service.add(3, 0, "hellow world");
		Assertions.assertThat(id).isLessThan(100).isGreaterThan(0);
		Assertions.assertThat(discuz.getFloor()).isEqualTo(2);

		Discussion one = service.getOne(id);
		Assertions.assertThat(one.getTime()).isAfter(LocalDateTime.MIN);
		Assertions.assertThat(one.getVoteCount()).isZero();
		Assertions.assertThat(one.isDeleted()).isFalse();
	}

	@Test
	void testGetList() {
		DiscussionQuery query = new DiscussionQuery();
		query.setUserId(0);
//		query.setCount(2);

		//delete查询条件
		Assertions.assertThat(service.count(query)).isEqualTo(4);
		query.setDeletion(DeletedState.ALL);
		Assertions.assertThat(service.count(query)).isEqualTo(5);

		Assertions.assertThat(service.getList(query)).size().isEqualTo(2);
	}

	@Test
	void testDeleteAndRestore() {
		service.delete(1);
		Assertions.assertThat(service.count(DiscussionQuery.byUser(0))).isEqualTo(3);
		service.restore(2); //对未删除的恢复操作是否失败？
		service.restore(3);
		Assertions.assertThat(service.count(DiscussionQuery.byUser(0))).isEqualTo(4);
	}

	@Test
	void testVote() {
		service.voteUp(1);
		Assertions.assertThatThrownBy(() -> service.voteUp(1)).isInstanceOf(ResourceStateException.class);
		Assertions.assertThat(service.getOne(1).getVoteCount()).isEqualTo(1);
	}
}
