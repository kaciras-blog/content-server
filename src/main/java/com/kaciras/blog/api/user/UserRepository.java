package com.kaciras.blog.api.user;

import com.kaciras.blog.api.Utils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.Clock;

/**
 * 用户存储，也没啥复杂的逻辑就是增改查，连删都没有，因为其它地方都在用，删了很难保证完整性。
 *
 * <h2>内置用户</h2>
 * 如果用 null 来表示匿名用户，虽然前端判断代码能少点字，但需要做判断的地方反而更多；
 * 另外 null 这个特殊值却只有一个，如果有多个特殊用户需要本地化，则无法实现。
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
