package net.kaciras.blog.api.article;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
interface KeywordDAO {

	@Select("SELECT value FROM keyword WHERE id=#{id}")
	List<String> select(int id);

	@Insert("INSERT INTO keyword(id, value) VALUES (#{id}, #{value})")
	void insert(@Param("id") int id, @Param("value") String value);

	@Delete("DELETE FROM keyword WHERE id=#{id}")
	void clear(int id);
}
