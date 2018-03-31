package net.kaciras.blog.domain.permission;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
interface RoleDAO {

	@Insert("INSERT INTO Role(name) VALUES(#{name})")
	@Options(useGeneratedKeys = true, keyColumn = "id")
	void insert(Role role);

	/**
	 * 手动插入，能够指定id，以及其他由数据库自动生成的字段。
	 *
	 * @param role 角色对象
	 */
	@Insert("INSERT INTO Role(id, name) VALUES(#{id}, #{name})")
	void insertManually(Role role);

	@Insert("INSERT INTO RoleInclude(id, include) VALUES(#{id}, #{include})")
	void insertInclude(@Param("id") int id, @Param("include") int include);

	@Delete("DELETE FROM Role WHERE id=#{id};" +
			"DELETE FROM RoleInclude WHERE include=#{id} OR id=#{id};")
	int delete(int id);

	@Delete("DELETE FROM RoleInclude WHERE id=#{id}")
	void deleteIncludes(int id);

	@Update("UPDATE Role SET name=#{name} WHERE id=#{id}")
	int updateAttribute(Role role);

	@Select("SELECT * FROM Role WHERE id=#{id}")
	Role selectAttribute(int id);

	@Select("SELECT include FROM RoleInclude WHERE id=#{id}")
	List<Integer> selectIncludes(int id);

	@Select("SELECT * FROM Role")
	List<Role> selectAll();
}
