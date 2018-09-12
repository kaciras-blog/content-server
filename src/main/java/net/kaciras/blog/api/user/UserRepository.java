package net.kaciras.blog.api.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.sql.DBUtils;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
class UserRepository {

	private final User guest = new User(0, "游客");

	private final UserDao userDao;

	public void add(User user) {
		userDao.insert(user);
	}

	public User get(int id) {
		if (id == 0) {
			return guest;
		}
		return userDao.select(id);
	}

	public void delete(int id) {
		DBUtils.checkEffective(userDao.delete(id));
	}

	public User getByName(String name) {
		return userDao.selectByName(name);
	}
}
