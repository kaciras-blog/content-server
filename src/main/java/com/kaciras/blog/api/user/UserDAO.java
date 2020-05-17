package com.kaciras.blog.api.user;

import org.apache.ibatis.annotations.*;

@Mapper
interface UserDAO {

	@Insert("INSERT INTO `user`(id, `name`, avatar, auth, reg_ip) " +
			"VALUES(#{id}, #{name}, #{avatar}, #{authType}, #{registerIP})")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	void insert(User user);

	@Select("SELECT * FROM `user` WHERE id=#{id}")
	@ResultMap("com.kaciras.blog.api.user.UserDAO.userMap")
	User select(int id);

	@Delete("UPDATE `user` SET deleted=1 WHERE id=#{id}")
	int delete(int id);

	@Update("UPDATE `user` SET `name`=#{name}, avatar=#{avatar} WHERE id=#{id}")
	int updateProfile(User user);
}
