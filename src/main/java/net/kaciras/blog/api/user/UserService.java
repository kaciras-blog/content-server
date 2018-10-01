package net.kaciras.blog.api.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.SecurtyContext;
import net.kaciras.blog.api.perm.Authenticator;
import net.kaciras.blog.api.perm.RequirePrincipal;
import net.kaciras.blog.api.perm.WebPrincipalType;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import net.kaciras.blog.infrastructure.sql.DBUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository repository;
	private final UserMapper mapper;
	private final RestTemplate restTemplate;

	private Authenticator authenticator;

	@Qualifier("UserAuthenticator")
	@Autowired
	public void setAuthenticator(Authenticator authenticator) {
		this.authenticator = authenticator;
	}

	public UserVo getUser(int id) {
		return mapper.toUserVo(DBUtils.checkNotNullResource(repository.get(id)));
	}

	@RequirePrincipal(value = WebPrincipalType.Logined, ex = ResourceNotFoundException.class)
	public UserVo getOrCreate(int id) {
		var user = repository.get(id);
		if (user != null) {
			return mapper.toUserVo(user);
		}
		SecurtyContext.requireId(id);
		user = restTemplate.getForObject("http://localhost:26481/accounts/{id}", User.class, id);
		user.setHead(ImageRefrence.parse("noface.gif"));
		repository.add(user);
		return mapper.toUserVo(user);
	}

	@RequirePrincipal
	public int ban(int id, long seconds, String cause) {
		return repository.get(id).ban(SecurtyContext.getUserId(), Duration.ofSeconds(seconds), cause);
	}

	@RequirePrincipal
	public void unban(int id, int bid, String cause) {
		repository.get(id).unBan(bid, SecurtyContext.getUserId(), cause);
	}

	public List<BanRecord> getBanRedords(int id) {
		SecurtyContext.requireId(id);
		return repository.get(id).getBanRecords();
	}
}
