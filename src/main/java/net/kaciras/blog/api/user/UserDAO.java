package net.kaciras.blog.api.user;

import org.apache.ibatis.annotations.*;

@Mapper
interface UserDAO {

	@Insert("INSERT INTO `user`(id, name, head, auth, reg_ip) " +
			"VALUES(#{id}, #{name}, #{head}, #{authType}, #{registerIP})")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	void insert(User user);

	@Select("SELECT * FROM `user` WHERE id=#{id}")
	@ResultMap("net.kaciras.blog.api.user.UserMap")
	User select(int id);

	@Delete("UPDATE `user` SET deleted=1 WHERE id=#{id}")
	int delete(int id);

	@Update("UPDATE `user` SET name=#{name}, head=#{head} WHERE id=#{id}")
	int updateProfile(User user);
}
