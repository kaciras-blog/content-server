package net.kaciras.blog.api.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.principal.AuthType;
import net.kaciras.blog.infrastructure.codec.ImageReference;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.net.InetAddress;

@RequiredArgsConstructor
@Service
public class UserManager {

	private final UserRepository repository;
	private final UserMapper mapper;

	/**
	 * 获取指定ID用户的信息。
	 * 该视图是面向公共的，一些敏感字段必须过滤掉。
	 *
	 * @param id 用户ID
	 * @return 用户信息
	 */
	public UserVo getUser(int id) {
		var user = repository.get(id);
		if (user == null) {
			throw new ResourceNotFoundException("User[id=" + id + "] 不存在");
		}
		var view = mapper.toUserVo(user);
		view.setAuthType(null);
		return view;
	}

	public int createNew(String name, AuthType authType, InetAddress ip) {
		var user = new User();
		user.setName(name);
		user.setHead(ImageReference.parse("akalin.jpg"));
		user.setAuthType(authType);
		user.setRegisterIP(ip);

		repository.add(user);
		return user.getId();
	}
}
