package net.kaciras.blog.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.Authenticator;
import net.kaciras.blog.SecurtyContext;
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
	private final PojoMapper mapper;
	private final RestTemplate restTemplate;

	private Authenticator authenticator;

	@Qualifier("UserAuthenticator")
	@Autowired
	public void setAuthenticator(Authenticator authenticator) {
		this.authenticator = authenticator;
	}

	public UserVo getUser(int id) {
		var user = repository.get(id);
		if(user != null) {
			return mapper.toUserVo(user);
		}
		return mapper.toUserVo(user);
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
		if(SecurtyContext.isNotUser(id)) {
			authenticator.require("POWER_QUERY");
		}
		return repository.get(id).getBanRecords();
	}
}
