package net.kaciras.blog.api.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.Authenticator;
import net.kaciras.blog.api.SecurtyContext;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
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
		return getUser(id, false);
	}

	public UserVo getUser(int id, boolean create) {
		var user = repository.get(id);
		if (user != null) {
			return mapper.toUserVo(user);
		}
		if (create) {
			SecurtyContext.requireUser(id);
			user = restTemplate.getForObject("http://localhost:26481/accounts/{id}", User.class, id);
			user.setHead(ImageRefrence.parse("noface.gif"));
			repository.add(user);
			return mapper.toUserVo(user);
		}
		throw new ResourceNotFoundException();
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
		if (SecurtyContext.isNotUser(id)) {
			authenticator.require("POWER_QUERY");
		}
		return repository.get(id).getBanRecords();
	}
}
