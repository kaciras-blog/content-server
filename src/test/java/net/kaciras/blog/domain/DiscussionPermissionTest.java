package net.kaciras.blog.domain;

import net.kaciras.blog.api.discuss.DiscussionQuery;
import net.kaciras.blog.api.discuss.DiscussionService;
import net.kaciras.blog.infrastructure.exception.PermissionException;
import net.kaciras.blog.api.DeletedState;
import net.kaciras.blog.api.SecurtyContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DiscussionPermissionTest extends AbstractSpringTest {

	@Autowired
	private DiscussionService service;

	@BeforeEach
	void setUp() {
		SecurtyContext.setCurrentUser(null);
	}

	@Test
	void testGetList() {
		DiscussionQuery query = new DiscussionQuery();
		query.setCount(20);

		query.setDeletion(DeletedState.ALL);
		Assertions.assertThatThrownBy(() -> service.getList(query)).isInstanceOf(PermissionException.class);

		SecurtyContext.setCurrentUser(2);
		Assertions.assertThatThrownBy(() -> service.getList(query)).isInstanceOf(PermissionException.class);

		query.setUserId(2);
		service.getList(query);
	}

	@Test
	void testDeleteAndRestore() {
		Assertions.assertThatThrownBy(() -> service.delete(1)).isInstanceOf(PermissionException.class);

		SecurtyContext.setCurrentUser(2);
		Assertions.assertThatThrownBy(() -> service.delete(5)).isInstanceOf(PermissionException.class);

		service.delete(8);

		Assertions.assertThatThrownBy(() -> service.restore(8)).isInstanceOf(PermissionException.class);
	}

	@Test
	void testGetOne() {
		Assertions.assertThatThrownBy(() -> service.getOne(9)).isInstanceOf(PermissionException.class);

		SecurtyContext.setCurrentUser(1);
		service.getOne(9);
	}
}
