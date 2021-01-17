package com.kaciras.blog.api.account.local;

import com.kaciras.blog.api.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@RequiredArgsConstructor
@Repository
public class AccountRepository {

	private final AccountDAO accountDAO;

	public void add(Account account) throws SQLException {
		accountDAO.insert(account);
	}

	@NonNull
	public Account findById(int id) {
		return Utils.checkNotNullResource(accountDAO.select(id));
	}

	public Account findByName(String name) {
		return accountDAO.selectByName(name);
	}
}
