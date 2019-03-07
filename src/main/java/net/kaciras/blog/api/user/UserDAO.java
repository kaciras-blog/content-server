package net.kaciras.blog.api.user;

import net.kaciras.blog.infrastructure.codec.ImageRefrence;
import org.apache.ibatis.annotations.*;

@Mapper
interface UserDAO {

	@Insert("INSERT INTO `user`(id, name, head, reg_ip) " +
			"VALUES(#{id}, #{name},#{head}, ${registerIP})")
	void insert(User user);

	@Select("SELECT id,name,head,deleted FROM `user` WHERE id=#{id}")
	@ResultMap("net.kaciras.blog.security.resultmap.AccountMap")
	User select(int id);

	@Select("SELECT id,name,deleted FROM `user` WHERE name=#{name}")
	@ResultMap("net.kaciras.blog.security.resultmap.AccountMap")
	User selectByName(String name);

	@Delete("UPDATE `user` SET deleted=1 WHERE id=#{id}")
	int delete(int id);

	@Update("UPDATE `user` SET head=#{head} WHERE id=#{id}")
	void updateHead(ImageRefrence head);
}
