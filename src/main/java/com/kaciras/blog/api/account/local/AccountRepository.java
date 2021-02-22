package com.kaciras.blog.api.account.local;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class AccountRepository {

	private final AccountDAO accountDAO;

	/**
	 * 像数据库添加一个新的账号。
	 *
	 * @param account 账号
	 * @throws DuplicateKeyException 如果账号重名
	 */
	public void add(Account account) {
		accountDAO.insert(account);
	}

	public Account findByName(String name) {
		return accountDAO.selectByName(name);
	}
}
