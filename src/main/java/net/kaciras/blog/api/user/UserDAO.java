package net.kaciras.blog.api.user;

import net.kaciras.blog.infrastructure.codec.ImageRefrence;
import org.apache.ibatis.annotations.*;

@Mapper
interface UserDAO {

	@Insert("INSERT INTO `user`(id, name, head) VALUES(#{id}, #{name},#{head})")
	void insert(User user);

	@Select("SELECT id,name,head,deleted FROM `user` WHERE id=#{id}")
	User select(int id);

	@Select("SELECT id,name,deleted FROM `user` WHERE name=#{name}")
	User selectByName(String name);

	@Update("UPDATE `user` SET head=#{head} WHERE id=#{id}")
	void updateHead(ImageRefrence head);

	@Delete("UPDATE `user` SET deleted=1 WHERE id=#{id}")
	int delete(int id);
}
