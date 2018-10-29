package net.kaciras.blog.api.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.DBUtils;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
class UserRepository {

	private final UserDAO userDao;

	public void add(User user) {
		userDao.insert(user);
	}

	public User get(int id) {
		if (id == 0) {
			return User.GUEST;
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
