package net.kaciras.blog.api;

import net.kaciras.blog.api.user.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
interface UserDAO {

	@Insert("INSERT INTO `user`(id, name, head) VALUES(#{id}, #{name},#{head})")
	void insert(User user);

	@Select("SELECT id,name,head,deleted FROM `user` WHERE id=#{id}")
	User select(int id);

	@Select("SELECT id,name,deleted FROM `user` WHERE name=#{name}")
	User selectByName(String name);

	@Delete("UPDATE `user` SET deleted=1 WHERE id=#{id}")
	int delete(int id);
}
