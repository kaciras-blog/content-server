package net.kaciras.blog.api.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.Utils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

// 未提供删除方法
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Repository
public class UserRepository {

	private final UserDAO userDAO;

	public void add(@NonNull User user) {
		userDAO.insert(user);
	}

	public User get(int id) {
		if (id == 0) {
			return User.GUEST;
		}
		return userDAO.select(id);
	}

	public void update(User user) {
		Utils.checkEffective(userDAO.updateProfile(user));
	}
}
