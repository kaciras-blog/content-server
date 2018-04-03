package net.kaciras.blog.domain.user;

import org.apache.ibatis.annotations.*;

@Mapper
interface UserDao {

	@Insert("INSERT INTO User(name,password,salt,head,regist_ip) " +
			"VALUES(#{name},#{password},#{salt},#{head},#{regAddress})")
	@Options(useGeneratedKeys = true, keyColumn = "id")
	void insert(User user);

	@Insert("INSERT INTO User(id,name,password,salt,head,regist_ip) " +
			"VALUES(#{id},#{name},#{password},#{salt},#{salt},#{regAddress})")
	void insertWithId(User user);

	@Select("SELECT id,name,head,regist_ip,regist_time FROM User WHERE id=#{id}")
	User select(int id);

	@Select("SELECT id,name,password,salt FROM User WHERE name=#{name}")
	User selectByName(String name);

	@Update("UPDATE User SET password=#{password},salt=#{salt} WHERE id=#{id}")
	int updateLoginInfo(User user);

	@Delete("DELETE FROM User WHERE id=#{id}")
	int delete(int id);
}
