package net.kaciras.blog.api.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import net.kaciras.blog.infrastructure.sql.DBUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class UserManager {

	private final UserRepository repository;
	private final UserMapper mapper;
	private final RestTemplate restTemplate;

	public UserVo getUser(int id) {
		return mapper.toUserVo(DBUtils.checkNotNullResource(repository.get(id)));
	}

	// 自动创建用户
	public UserVo ensureCurrent() {
		var principal = SecurityContext.getPrincipal();
		if (!principal.isLogined()) {
			throw new ResourceNotFoundException();
		}
		var user = repository.get(principal.getId());
		if (user != null) {
			return mapper.toUserVo(user);
		}

		user = restTemplate.getForObject("https://localhost:26480/accounts/{id}",
				User.class, principal.getId());
		user.setHead(ImageRefrence.parse("noface.gif"));
		repository.add(user);
		return mapper.toUserVo(user);
	}
}
