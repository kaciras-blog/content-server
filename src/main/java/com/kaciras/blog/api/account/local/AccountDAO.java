package com.kaciras.blog.api.account.local;

import org.apache.ibatis.annotations.*;

@Mapper
interface AccountDAO {

	@Insert("INSERT INTO account (id, name, password, salt) VALUES (#{id}, #{name}, #{password}, #{salt})")
	@Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
	void insert(Account account);

	@Select("SELECT id,name FROM account WHERE id=#{id}")
	Account select(int id);

	@Select("SELECT id,name,password,salt FROM account WHERE name=#{name}")
	Account selectByName(String name);

	@Update("UPDATE account SET password=#{password},salt=#{salt} WHERE id=#{id}")
	void updatePassword(Account account);
}
