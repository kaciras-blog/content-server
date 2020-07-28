package com.kaciras.blog.api.friend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;

import static com.kaciras.blog.api.friend.TestHelper.createFriend;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
final class FriendRepositoryTest {

	@Autowired
	private RedisConnectionFactory redis;

	@Autowired
	private FriendRepository repository;

	@BeforeEach
	void flushDb() {
		redis.getConnection().flushDb();
	}

	@Test
	void addRepeat() {
		var friend = createFriend("example.com");
		assertThat(repository.addFriend(friend)).isTrue();
		assertThat(repository.addFriend(friend)).isFalse();
	}

	@Test
	void add() {
		var friend = createFriend("example.com");
		repository.addFriend(friend);

		var rv = repository.getFriends()[0];

		// 忽略Json序列化精度问题
		rv.createTime = friend.createTime;

		assertThat(friend.createTime).isNotNull();
		assertThat(rv).isEqualToComparingFieldByField(friend);
	}

	@Test
	void removeNonExists() {
		assertThat(repository.remove("example.com")).isFalse();
	}

	@Test
	void remove() {
		repository.addFriend(createFriend("A"));
		repository.addFriend(createFriend("B"));
		repository.addFriend(createFriend("C"));

		assertThat(repository.remove("B")).isTrue();

		var friends = repository.getFriends();
		assertThat(friends).hasSize(2);
		assertThat(friends[0].name).isEqualTo("A");
		assertThat(friends[1].name).isEqualTo("C");
	}

	@Test
	void sort() {
		repository.addFriend(createFriend("A"));
		repository.addFriend(createFriend("B"));
		repository.addFriend(createFriend("C"));

		repository.updateSort(new String[]{"C", "A", "B"});

		var friends = repository.getFriends();
		assertThat(friends).hasSize(3);
		assertThat(friends[0].name).isEqualTo("C");
		assertThat(friends[1].name).isEqualTo("A");
		assertThat(friends[2].name).isEqualTo("B");
	}
}
