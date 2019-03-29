package net.kaciras.blog.api.principle.local;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.DBUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@RequiredArgsConstructor
@Repository
public class AccountRepository {

	private final AccountDAO accountDAO;

	// accountDAO 是由 mybatis 自动实现的会抛出 SQLException
	@SuppressWarnings("RedundantThrows")
	public void add(Account account) throws SQLException {
		accountDAO.insert(account);
	}

	@NonNull
	public Account findById(int id) {
		return DBUtils.checkNotNullResource(accountDAO.select(id));
	}

	public Account findByName(String name) {
		return accountDAO.selectByName(name);
	}
}
