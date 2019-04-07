package net.kaciras.blog.api.user;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.principle.AuthType;
import net.kaciras.blog.infrastructure.DBUtils;
import net.kaciras.blog.infrastructure.codec.ImageReference;
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
		var view = mapper.toUserVo(DBUtils.checkNotNullResource(repository.get(id)));
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
