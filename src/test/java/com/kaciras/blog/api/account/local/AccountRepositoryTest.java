package com.kaciras.blog.api.account.local;

import com.kaciras.blog.api.MinimumSpringTest;
import com.kaciras.blog.api.UseBlogMybatis;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(AccountRepository.class)
@UseBlogMybatis
@MinimumSpringTest
class AccountRepositoryTest {

	@Autowired
	private AccountRepository repository;

	private Account stub(int id, String name) {
		var account = new Account();
		account.setId(id);
		account.setName(name);
		account.setSalt(new byte[64]);
		account.setPassword(new byte[64]);
		return account;
	}

	@Test
	void nameConflict() {
		var a0 = stub(3, "alice");
		var a1 = stub(4, "alice");
		repository.add(a0);
		assertThatThrownBy(() -> repository.add(a1)).isInstanceOf(DuplicateKeyException.class);
	}

	@Test
	void findByName() {
		var account = stub(3, "bob");
		repository.add(account);

		var result = repository.findByName("bob");
		assertThat(result).usingRecursiveComparison().isEqualTo(account);
	}
}
