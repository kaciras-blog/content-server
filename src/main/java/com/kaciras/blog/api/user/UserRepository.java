package com.kaciras.blog.api.user;

import com.kaciras.blog.api.Utils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.Clock;

/**
 * 用户存储，也没啥复杂的逻辑就是增改查，连删都没有，
 * 因为用户好删除，其它地方都在用，删了很难保证完整性。
 */
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
