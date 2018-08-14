package net.kaciras.blog.domain.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.SecurtyContext;
import net.kaciras.blog.domain.Authenticator;
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
	private final RestTemplate restTemplate;

	private Authenticator authenticator;

	@Qualifier("UserAuthenticator")
	@Autowired
	public void setAuthenticator(Authenticator authenticator) {
		this.authenticator = authenticator;
	}

	public User getUser(int id) {
		var user = repository.get(id);
		if(user != null) {
			return user;
		}
		return user;
	}

	public int ban(int id, long seconds, String cause) {
		authenticator.require("BAN");
		return getUser(id).ban(SecurtyContext.getRequiredCurrentUser(), Duration.ofSeconds(seconds), cause);
	}

	public void unban(int id, int bid, String cause) {
		authenticator.require("BAN");
		getUser(id).unBan(bid, SecurtyContext.getRequiredCurrentUser(), cause);
	}

	public List<BanRecord> getBanRedords(int id) {
		if(SecurtyContext.isNotUser(id)) {
			authenticator.require("POWER_QUERY");
		}
		return getUser(id).getBanRecords();
	}
}
