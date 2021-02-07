package com.kaciras.blog.api.user;

import org.apache.ibatis.annotations.*;

@Mapper
interface UserDAO {

	@Insert("INSERT INTO `user`(id, `name`, avatar, email, auth, create_ip) " +
			"VALUES(#{id}, #{name}, #{avatar}, #{email}, #{auth}, #{createIP})")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	void insert(User user);

	@Select("SELECT * FROM `user` WHERE id=#{id}")
	User select(int id);

	@Delete("UPDATE `user` SET deleted=1 WHERE id=#{id}")
	int delete(int id);

	@Update("UPDATE `user` SET `name`=#{name}, avatar=#{avatar} WHERE id=#{id}")
	int updateProfile(User user);
}
