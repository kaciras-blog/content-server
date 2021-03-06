package com.kaciras.blog.api.user;

import org.apache.ibatis.annotations.*;

@Mapper
interface UserDAO {

	@Insert("INSERT INTO `user`(`name`, avatar, email, auth, create_time, create_ip) " +
			"VALUES(#{name}, #{avatar}, #{email}, #{auth}, #{createTime}, #{createIP})")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	void insert(User user);

	@Select("SELECT * FROM `user` WHERE id=#{id}")
	User select(int id);

	@Delete("UPDATE `user` SET deleted=1 WHERE id=#{id}")
	int delete(int id);

	@Update("UPDATE `user` SET `name`=#{name},avatar=#{avatar},email=#{email} WHERE id=#{id}")
	int updateProfile(User user);
}
