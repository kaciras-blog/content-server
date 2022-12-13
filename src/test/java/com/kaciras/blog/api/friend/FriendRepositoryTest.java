package com.kaciras.blog.api.friend;

import com.kaciras.blog.api.MinimumSpringTest;
import com.kaciras.blog.api.UseBlogRedis;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.net.URI;

import static com.kaciras.blog.api.friend.TestHelper.createFriend;
import static org.assertj.core.api.Assertions.assertThat;

@Import(FriendRepository.class)
@UseBlogRedis
@MinimumSpringTest
final class FriendRepositoryTest {

	@Autowired
	private FriendRepository repository;

	@Test
	void addRepeat() {
		var friend = createFriend("example.com");
		assertThat(repository.add(friend)).isTrue();
		assertThat(repository.add(friend)).isFalse();
	}

	@Test
	void add() {
		var friend = createFriend("example.com");
		repository.add(friend);

		var rv = repository.getAll()[0];
		assertThat(friend.createTime).isNotNull();

		// 忽略 Json 序列化精度问题，下同
		assertThat(rv).usingRecursiveComparison().ignoringFields("createTime").isEqualTo(friend);
	}

	@Test
	void findByHost() {
		var friend = createFriend("example.com");
		repository.add(friend);

		assertThat(repository.findByHost("non.exists")).isNull();

		var found = repository.findByHost("example.com");
		assertThat(found).isNotNull();
		assertThat(found).usingRecursiveComparison().ignoringFields("createTime").isEqualTo(friend);
	}

	@Test
	void removeNonExists() {
		assertThat(repository.remove("example.com")).isFalse();
	}

	@Test
	void remove() {
		repository.add(createFriend("A"));
		repository.add(createFriend("B"));
		repository.add(createFriend("C"));

		assertThat(repository.remove("B")).isTrue();

		var friends = repository.getAll();
		assertThat(friends).hasSize(2);
		assertThat(friends[0].name).isEqualTo("A");
		assertThat(friends[1].name).isEqualTo("C");
	}

	@Test
	void updateNonExists() {
		var success = repository.update("no-exists", createFriend("test"));
		assertThat(success).isFalse();
	}

	@Test
	void update() {
		var old = createFriend("A", "foo", null);
		repository.add(old);
		repository.add(createFriend("B"));
		repository.add(createFriend("C"));

		var success = repository.update("A", createFriend("A", "bar", null));
		assertThat(success).isTrue();

		var friends = repository.getAll();
		assertThat(friends).hasSize(3);
		assertThat(friends[0].url).isEqualTo(old.url);
		assertThat(friends[0].friendPage).isEqualTo(URI.create("bar"));
	}

	@Test
	void updateWithHost() {
		var old = createFriend("A", "foo", null);
		var new_ = createFriend("xx", "bar", null);

		repository.add(old);
		repository.add(createFriend("B"));
		repository.add(createFriend("C"));

		var success = repository.update("A", new_);
		assertThat(success).isTrue();

		var friends = repository.getAll();
		assertThat(friends).hasSize(3);
		assertThat(friends[0].url).isEqualTo(new_.url);
		assertThat(friends[0].name).isEqualTo("xx");
		assertThat(friends[0].friendPage).isEqualTo(URI.create("bar"));
	}

	@Test
	void sort() {
		repository.add(createFriend("A"));
		repository.add(createFriend("B"));
		repository.add(createFriend("C"));

		repository.updateSort(new String[]{"C", "A", "B"});

		var friends = repository.getAll();
		assertThat(friends).hasSize(3);
		assertThat(friends[0].name).isEqualTo("C");
		assertThat(friends[1].name).isEqualTo("A");
		assertThat(friends[2].name).isEqualTo("B");
	}

	/**
	 * 更新两遍，确认幂等性（BoundKeyOperations.rename 的坑）
	 */
	@Test
	void sortIdempotence() {
		repository.add(createFriend("A"));
		repository.add(createFriend("B"));

		repository.updateSort(new String[]{"B", "A"});
		repository.updateSort(new String[]{"B", "A"});

		assertThat(repository.getAll()).hasSize(2);
	}
}
