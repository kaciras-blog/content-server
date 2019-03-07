package net.kaciras.blog.api.principle.local;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.DBUtils;
import net.kaciras.blog.infrastructure.exception.ResourceDeletedException;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
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

	/**
	 * 并不会真的删除，而是设置删除标记。被标记的用户不可登录。
	 *
	 * @param id 用户id
	 * @throws ResourceNotFoundException 用户不存在
	 * @throws ResourceDeletedException  用户已被标记为删除
	 */
	public void delete(int id) {
		if (accountDAO.updateDeleted(id) > 0) {
			return;
		}
		if (accountDAO.select(id) == null) {
			throw new ResourceNotFoundException();
		}
		throw new ResourceDeletedException();
	}
}
