package com.kaciras.blog.api.user;

import com.kaciras.blog.api.Utils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.Clock;

// 未提供删除方法
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Repository
public class UserRepository {

	private final UserDAO userDAO;
	private final Clock clock;

	public void add(@NonNull User user) {
		user.setCreateTime(clock.instant());
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
