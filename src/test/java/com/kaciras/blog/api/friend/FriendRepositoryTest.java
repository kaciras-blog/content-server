package com.kaciras.blog.api.friend;

import com.kaciras.blog.infra.codec.ImageReference;
import com.kaciras.blog.infra.exception.ResourceStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

	private FriendLink createFriend(String name) {
		var url = "https://" + name;
		var image = ImageReference.parse("test.png");
		return new FriendLink(url, name, image, image, null, null);
	}

	@Test
	void addRepeat() {
		repository.addFriend(createFriend("example.com"));
		assertThatThrownBy(() -> repository.addFriend(createFriend("example.com")))
				.isInstanceOf(ResourceStateException.class);
	}

	@Test
	void add() {
		var friend = createFriend("example.com");
		repository.addFriend(friend);
		assertThat(repository.getFriends()[0]).isEqualToComparingFieldByField(friend);
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
