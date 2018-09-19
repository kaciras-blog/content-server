package net.kaciras.blog.api.user;

import org.apache.ibatis.annotations.*;

@Mapper
interface UserDAO {

	@Insert("INSERT INTO `user`(name,head) VALUES(#{name},#{head})")
	@Options(useGeneratedKeys = true, keyColumn = "id")
	void insert(User user);

	@Select("SELECT id,name,head,deleted FROM `user` WHERE id=#{id}")
	User select(int id);

	@Select("SELECT id,name,deleted FROM `user` WHERE name=#{name}")
	User selectByName(String name);

	@Delete("UPDATE `user` SET deleted=1 WHERE id=#{id}")
	int delete(int id);
}
