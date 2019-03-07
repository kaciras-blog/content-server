package net.kaciras.blog.api.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.DBUtils;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserManager {

	private final UserRepository repository;
	private final UserMapper mapper;

	// TODO: 在哪转换对象
	public UserVo getUser(int id) {
		return mapper.toUserVo(DBUtils.checkNotNullResource(repository.get(id)));
	}

	public int createNewUser(String name) {
		var user = new User();
		user.setName(name);
		user.setHead(ImageRefrence.parse("akalin.jpg"));
		repository.add(user);
		return user.getId();
	}
}
