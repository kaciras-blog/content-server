package net.kaciras.blog.domain.user;

import org.apache.ibatis.annotations.*;

@Mapper
interface UserDao {

	@Insert("INSERT INTO User(name,head) VALUES(#{name},#{head})")
	@Options(useGeneratedKeys = true, keyColumn = "id")
	void insert(User user);

	@Select("SELECT id,name,head,deleted FROM User WHERE id=#{id}")
	User select(int id);

	@Select("SELECT id,name,deleted FROM User WHERE name=#{name}")
	User selectByName(String name);

	@Delete("UPDATE User SET deleted=1 WHERE id=#{id}")
	int delete(int id);
}
