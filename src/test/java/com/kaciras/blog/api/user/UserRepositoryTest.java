package com.kaciras.blog.api.user;

import com.kaciras.blog.api.MinimumSpringTest;
import com.kaciras.blog.api.UseBlogMybatis;
import com.kaciras.blog.api.account.AuthType;
import com.kaciras.blog.infra.codec.ImageReference;
import com.kaciras.blog.infra.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.net.InetAddress;
import java.time.Clock;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@Import(UserRepository.class)
@UseBlogMybatis
@MinimumSpringTest
class UserRepositoryTest {

	@Autowired
	private UserRepository repository;

	@MockBean
	private Clock clock;

	private User testUser() {
		var user = new User();
		user.setEmail("alice@example.com");
		user.setCreateIP(InetAddress.getLoopbackAddress());
		user.setAuth(AuthType.LOCAL);
		user.setName("alice");
		user.setAvatar(ImageReference.parse("3IeQaaHXqjt8kQ675nCT.svg"));
		return user;
	}

	@Test
	void getGuest() {
		assertThat(repository.get(0)).isEqualTo(User.GUEST);
	}

	@Test
	void getNonExists() {
		assertThat(repository.get(666)).isNull();
	}

	// 懒得拆俩方法，搁着里一起测了算了
	@Test
	void addAndGet() {
		var user = testUser();
		when(clock.instant()).thenReturn(Instant.EPOCH);

		repository.add(user);
		assertThat(user.getId()).isGreaterThan(0);
		assertThat(user.getCreateTime()).isNotNull();

		var got = repository.get(user.getId());
		assertThat(got).usingRecursiveComparison().isEqualTo(user);
	}

	@Test
	void updateNonExists() {
		var user = new User();
		user.setId(666);
		assertThatThrownBy(() -> repository.update(user)).isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void update() {
		var user = testUser();
		when(clock.instant()).thenReturn(Instant.EPOCH);
		repository.add(user);

		user.setName("bob");
		user.setAvatar(null);
		user.setEmail("bob@example.com");
		repository.update(user);

		var got = repository.get(user.getId());
		assertThat(got).usingRecursiveComparison().isEqualTo(user);
	}
}
