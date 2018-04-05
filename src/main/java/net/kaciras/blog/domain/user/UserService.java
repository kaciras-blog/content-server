package net.kaciras.blog.domain.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.SecurtyContext;
import net.kaciras.blog.domain.permission.Authenticator;
import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class UserService {

	private final Pattern validate = Pattern.compile("^[\\u4E00-\\u9FFFa-zA-Z0-9_]{3,16}$");

	private final UserRepository repository;

	private Authenticator authenticator;

	@Qualifier("UserAuthenticator")
	@Autowired
	public void setAuthenticator(Authenticator authenticator) {
		this.authenticator = authenticator;
	}

	public User getUser(int id) {
		return repository.get(id);
	}

	public User login(String name, String password) {
		User user = repository.getByName(name);
		if (user == null || !user.checkLogin(password)) {
			return null;
		}
		return user;
	}

	public void changePassword(String newPassword) {
		Integer loginedUser = SecurtyContext.getCurrentUser();
		if(loginedUser == null) {
			throw new RequestArgumentException("用户不存在或没有修改的权限");
		}
		User user = repository.get(loginedUser);
		user.putPassword(newPassword);
		repository.update(user);
	}

	public User register(RegisterVo registerVo) {
		if (!validate.matcher(registerVo.getName()).find()) {
			throw new IllegalArgumentException("用户名长度不对，或有非法字符");
		}
		return repository.add(registerVo);
	}

	/**
	 * 此方法将真正地删除一个用户，被删除用户的所有相关数据（文章、评论等）
	 * 都将被设为删除，但是用户的记录仍然保留。使用此方法删除将无法撤销。
	 *
	 * @param id 用户id
	 */
	public void delete(int id) {
		authenticator.require("DELETE");
		repository.delete(id);
	}

	public int ban(int id, long seconds, String cause) {
		authenticator.require("BAN");
		return repository.get(id).ban(SecurtyContext.getRequiredCurrentUser(), Duration.ofSeconds(seconds), cause);
	}

	public void unban(int id, int bid, String cause) {
		authenticator.require("BAN");
		repository.get(id).unBan(bid, SecurtyContext.getRequiredCurrentUser(), cause);
	}

	public List<BanRecord> getBanRedords(int id) {
		if(SecurtyContext.getCurrentUser() != id) {
			authenticator.require("POWER_QUERY");
		}
		return repository.get(id).getBanRecords();
	}
}
