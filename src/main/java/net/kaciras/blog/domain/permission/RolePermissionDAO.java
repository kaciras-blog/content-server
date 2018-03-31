package net.kaciras.blog.domain.permission;

import org.apache.ibatis.annotations.*;

import java.util.Set;

@Mapper
public interface RolePermissionDAO {

	@Insert("INSERT INTO PermissionAssociate(id, include) VALUES (#{id}, #{perm})")
	void insert(@Param("id") int id, @Param("perm") String perm);

	@Delete("DELETE FROM PermissionAssociate WHERE id=#{id} AND include=#{perm}")
	int delete(@Param("id") int id, @Param("perm") String perm);

	@Delete("DELETE FROM PermissionAssociate WHERE id=#{id}")
	void deleteById(int id);

	@Delete("DELETE FROM PermissionAssociate WHERE include=#{perm}")
	void deleteByPermission(String perm);

	@Update("UPDATE PermissionAssociate SET include=#{New} WHERE include=#{old}")
	void updatePermission(@Param("old") String old, @Param("New") String New);

	@Select("SELECT 1 FROM PermissionAssociate WHERE id=#{id} AND include=#{perm}")
	Boolean checkPermission(@Param("id") int id, @Param("perm") String perm);

	@Select("SELECT include FROM PermissionAssociate WHERE id=#{id}")
	Set<PermissionKey> selectByRoleId(int id);
}
