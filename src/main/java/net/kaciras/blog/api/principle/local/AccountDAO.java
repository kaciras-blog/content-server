package net.kaciras.blog.api.principle.local;

import org.apache.ibatis.annotations.*;

@Mapper
interface AccountDAO {

	@Insert("INSERT INTO account(name, password, salt, regist_ip) " +
			"VALUES(#{name}, #{password}, #{salt}, #{registerAddress})")
	@Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
	void insert(Account account);

	@Select("SELECT id,name,deleted,regist_ip,regist_time FROM account WHERE id=#{id}")
	@ResultMap("net.kaciras.blog.security.resultmap.AccountMap")
	Account select(int id);

	@Select("SELECT id,name,password,salt,deleted FROM account WHERE name=#{name}")
	@ResultMap("net.kaciras.blog.security.resultmap.AccountMap")
	Account selectByName(String name);

	@Update("UPDATE account SET password=#{password},salt=#{salt} WHERE id=#{id}")
	void updatePassword(Account account);

	@Delete("UPDATE account SET deleted=1 WHERE id=#{id}")
	int updateDeleted(int id);
}
