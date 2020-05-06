package com.kaciras.blog.api.article;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
interface KeywordDAO {

	@Select("SELECT value FROM keyword WHERE id=#{id}")
	List<String> select(int id);

	@Insert("INSERT INTO keyword(id, value) VALUES (#{id}, #{value})")
	void insert(int id, String value);

	@Delete("DELETE FROM keyword WHERE id=#{id}")
	void clear(int id);
}
