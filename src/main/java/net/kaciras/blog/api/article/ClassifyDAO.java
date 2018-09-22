package net.kaciras.blog.api.article;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
interface ClassifyDAO {

	@Update("UPDATE article SET category=#{cid} WHERE id=#{aid}")
	void updateByArticle(@Param("aid") int aid, @Param("cid") int cid);

	@Update("UPDATE article SET category=#{New} WHERE category=#{old}")
	void updateCategory(@Param("old") int old, @Param("New") int New);

	@Select("SELECT category FROM article WHERE id=#{id}")
	int selectById(int id);

	// 感觉性能不太好
	@Select("SELECT COUNT(*) FROM article AS A " +
			"JOIN category_tree AS B ON A.category=B.descendant " +
			"WHERE deleted=0 AND B.ancestor=#{cid}")
	int selectCountByCategory(@Param("cid") int cid);
}
