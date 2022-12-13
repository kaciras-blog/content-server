package com.kaciras.blog.api.account.local;

import com.kaciras.blog.api.MinimumSpringTest;
import com.kaciras.blog.api.UseBlogMybatis;
import com.kaciras.blog.api.UseBlogRedis;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(AccountRepository.class)
@UseBlogMybatis
@UseBlogRedis
@MinimumSpringTest
class AccountRepositoryTest {

	@Autowired
	private AccountRepository repository;

	@Test
	void nameConflict() {
		var a0 = Account.create(3, "alice", "foobar2000");
		var a1 = Account.create(4, "alice", "foobar2000");
		repository.add(a0);
		assertThatThrownBy(() -> repository.add(a1)).isInstanceOf(DuplicateKeyException.class);
	}

	@Test
	void findByName() {
		var account = Account.create(3, "alice", "foobar2000");
		repository.add(account);

		var result = repository.findByName("alice");
		assertThat(result).usingRecursiveComparison().isEqualTo(account);
	}
}
