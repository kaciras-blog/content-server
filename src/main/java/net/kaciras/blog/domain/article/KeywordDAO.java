package net.kaciras.blog.domain.article;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface KeywordDAO {

	@Select("SELECT value FROM Keyword WHERE id=#{id}")
	List<String> select(int id);

	@Insert("INSERT INTO Keyword(id,value) VALUES (#{id}, #{value})")
	void insert(@Param("id") int id, @Param("value") String value);

	@Delete("DELETE FROM Keyword WHERE id=#{id}")
	void clear(int id);
}
