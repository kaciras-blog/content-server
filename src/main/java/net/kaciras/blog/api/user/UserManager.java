package net.kaciras.blog.api.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.DBUtils;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class UserManager {

	private final UserRepository repository;
	private final UserMapper mapper;
	private final RestTemplate restTemplate;

	// TODO: 在哪转换对象
	public UserVo getUser(int id) {
		return mapper.toUserVo(DBUtils.checkNotNullResource(repository.get(id)));
	}

	// 自动创建用户
	public User ensureCurrent() {
		var principal = SecurityContext.getPrincipal();
		if (!principal.isLogined()) {
			throw new ResourceNotFoundException();
		}
		var user = repository.get(principal.getId());
		if (user != null) {
			return user;
		}

		user = restTemplate.getForObject("https://localhost:26480/accounts/{id}", User.class, principal.getId());
		if (user == null) {
			throw new RuntimeException("Session中存在的用户ID，在SecurityCenter却查不出来");
		}
		user.setHead(ImageRefrence.parse("noface.gif"));
		repository.add(user);
		return user;
	}
}
