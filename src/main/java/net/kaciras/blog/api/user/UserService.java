package net.kaciras.blog.api.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.SecurityContext;
import net.kaciras.blog.api.perm.RequirePrincipal;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import net.kaciras.blog.infrastructure.sql.DBUtils;
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


	public UserVo getUser(int id) {
		return mapper.toUserVo(DBUtils.checkNotNullResource(repository.get(id)));
	}

	public UserVo ensureCurrent() {
		var principal = SecurityContext.getPrincipal();
		if (!principal.isLogined()) {
			throw new ResourceNotFoundException();
		}
		var user = repository.get(principal.getId());
		if (user != null) {
			return mapper.toUserVo(user);
		}
		user = restTemplate.getForObject("http://localhost:26481/accounts/{id}", User.class, principal.getId());
		user.setHead(ImageRefrence.parse("noface.gif"));
		repository.add(user);
		return mapper.toUserVo(user);
	}

	@RequirePrincipal
	public int ban(int id, long seconds, String cause) {
		return repository.get(id).ban(SecurityContext.getUserId(), Duration.ofSeconds(seconds), cause);
	}

	@RequirePrincipal
	public void unban(int id, int bid, String cause) {
		repository.get(id).unBan(bid, SecurityContext.getUserId(), cause);
	}

	public List<BanRecord> getBanRedords(int id) {
		SecurityContext.requireId(id);
		return repository.get(id).getBanRecords();
	}
}
