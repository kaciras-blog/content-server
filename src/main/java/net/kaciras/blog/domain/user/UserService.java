package net.kaciras.blog.domain.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.SecurtyContext;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class UserService {

	private Pattern validate = Pattern.compile("^[\\u4E00-\\u9FFFa-zA-Z0-9_]{3,16}$");

	private final UserRepository repository;

	public User login(String name, String password) {
		User user = repository.getByName(name);
		if (user == null || !user.checkLogin(password)) {
			return null;
		}
		return user;
	}

	public void changePassword(int id, String newPassword) {
		User user = repository.get(id);
		user.putPassword(newPassword);
		repository.update(user);
	}

	public User register(RegisterVo registerVo) {
		if (!validate.matcher(registerVo.getName()).find()) {
			throw new IllegalArgumentException("用户名长度不对，或有非法字符");
		}
		return repository.add(registerVo);
	}

	public void delete(int id) {
		SecurtyContext.checkAccept("AccountService", "MODIFY");
		repository.delete(id);
	}

	public User getUser(int id) {
		return repository.get(id);
	}
}
