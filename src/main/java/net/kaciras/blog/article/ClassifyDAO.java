package net.kaciras.blog.article;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
interface ClassifyDAO {

	@Update("UPDATE Article SET category=#{cid} WHERE id=#{aid}")
	void updateByArticle(@Param("aid") int aid, @Param("cid") int cid);

	@Update("UPDATE Article SET category=#{New} WHERE category=#{old}")
	void updateCategory(@Param("old") int old, @Param("New") int New);

	@Select("SELECT category FROM Article WHERE id=#{id}")
	List<Integer> selectById(int id);

	// 感觉性能不太好
	@Select("SELECT COUNT(*) FROM Article AS A JOIN CategoryTree AS B ON A.category=B.descendant WHERE deleted=0 AND B.ancestor=#{cid}")
	int selectCountByCategory(@Param("cid") int cid);
}
