package net.kaciras.blog.domain.permission;

import org.apache.ibatis.annotations.*;

import java.util.Set;

@CacheNamespace
@Mapper
interface UserRoleDAO {

	@Select("SELECT rid FROM UserRole WHERE uid=#{uid}")
	Set<Integer> selectUserRoles(int uid);

	@Insert("INSERT INTO UserRole(uid, rid) VALUES(#{uid},#{rid})")
	void insertUserRole(@Param("uid") int uid, @Param("rid") int rid);

	@Delete("DELETE FROM UserRole WHERE uid=#{uid} AND rid=#{rid}")
	int deleteUserRole(@Param("uid") int uid, @Param("rid") int rid);

	@Select("SELECT 1 FROM UserRole WHERE uid=#{uid} AND rid=#{rid}")
	Boolean selectHasRole(@Param("uid") int uid, @Param("rid") int rid);
}
