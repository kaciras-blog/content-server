package com.kaciras.blog.api.user;

import com.kaciras.blog.api.account.AuthType;
import com.kaciras.blog.infra.codec.ImageReference;
import com.kaciras.blog.infra.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.InetAddress;

@RequiredArgsConstructor
@Service
public class UserManager {

	private final UserRepository repository;
	private final UserMapper mapper;

	/**
	 * 获取指定 ID 用户的信息。
	 *
	 * @param id 用户ID
	 * @return 用户信息
	 */
	public UserVO getUser(int id) {
		var user = repository.get(id);
		if (user == null) {
			throw new ResourceNotFoundException("User[id=" + id + "] 不存在");
		}
		return mapper.toUserVo(user);
	}

	public int createNew(String name, AuthType authType, InetAddress ip) {
		var user = new User();
		user.setName(name);
		user.setAvatar(ImageReference.parse("akalin.jpg"));
		user.setAuth(authType);
		user.setCreateIP(ip);

		repository.add(user);
		return user.getId();
	}
}
