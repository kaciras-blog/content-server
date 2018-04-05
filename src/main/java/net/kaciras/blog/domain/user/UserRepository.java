package net.kaciras.blog.domain.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.Utils;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
class UserRepository {

	private User guest = new User(0, "游客");

	private final UserDao userDao;

	public User add(RegisterVo registerVo) {
		User user = new User();
		user.setName(registerVo.getName());
		user.setEmail(registerVo.getEmail());
		user.setRegAddress(registerVo.getRegAddress());
		user.putPassword(registerVo.getPassword());
		userDao.insert(user);
		return user;
	}

	public User get(int id) {
		if (id == 0) {
			return guest;
		}
		return userDao.select(id);
	}

	public void delete(int id) {
		Utils.checkEffective(userDao.delete(id));
	}

	public User getByName(String name) {
		return userDao.selectByName(name);
	}

	public void update(User user) {
		Utils.checkEffective(userDao.updateLoginInfo(user));
	}
}
