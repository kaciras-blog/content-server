package com.kaciras.blog.api.account.local;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
final class AccountRepositoryTest {

	@Autowired
	private AccountRepository repository;

	@Test
	void nameConflict() throws SQLException {
		var a0 = Account.create(3, "alice", "foobar2000");
		var a1 = Account.create(4, "alice", "foobar2000");
		repository.add(a0);
		assertThatThrownBy(() -> repository.add(a1)).isInstanceOf(DuplicateKeyException.class);
	}

	@Test
	void findByName(){
		var account = Account.create(3, "alice", "foobar2000");
		repository.add(account);

		var result = repository.findByName("alice");
		assertThat(result).usingRecursiveComparison().isEqualTo(account);
	}
}
