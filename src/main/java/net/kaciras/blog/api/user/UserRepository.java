package net.kaciras.blog.api.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

// 未提供删除方法
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Repository
public class UserRepository {

	private final UserDAO userDao;

	public void add(@NonNull User user) {
		userDao.insert(user);
	}

	public User get(int id) {
		if (id == 0) {
			return User.GUEST;
		}
		return userDao.select(id);
	}

	public User getByName(String name) {
		return userDao.selectByName(name);
	}
}
