package net.kaciras.blog.domain.user;

import org.apache.ibatis.annotations.*;

@Mapper
interface UserDao {

	@Insert("INSERT INTO User(name,password,salt,head,regist_ip) " +
			"VALUES(#{name},#{password},#{salt},#{head},#{regAddress})")
	@Options(useGeneratedKeys = true, keyColumn = "id")
	void insert(User user);

	@Select("SELECT id,name,head,deleted,regist_ip,regist_time FROM User WHERE id=#{id}")
	User select(int id);

	@Select("SELECT id,name,password,salt,deleted FROM User WHERE name=#{name}")
	User selectByName(String name);

	@Update("UPDATE User SET password=#{password},salt=#{salt} WHERE id=#{id}")
	int updateLoginInfo(User user);

	@Delete("UPDATE User SET deleted=1 WHERE id=#{id}")
	int delete(int id);
}
