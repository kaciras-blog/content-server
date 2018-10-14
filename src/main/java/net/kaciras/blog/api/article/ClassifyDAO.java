package net.kaciras.blog.api.article;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
interface ClassifyDAO {

	@Update("UPDATE article SET category=#{cid} WHERE id=#{aid}")
	void updateByArticle(int aid, int cid);

	@Update("UPDATE article SET category=#{New} WHERE category=#{old}")
	void updateCategory(int old, int New);

	@Select("SELECT category FROM article WHERE id=#{id}")
	int selectById(int id);

	// 感觉性能不太好
	@Select("SELECT COUNT(*) FROM article AS A " +
			"JOIN category_tree AS B ON A.category=B.descendant " +
			"WHERE deleted=0 AND B.ancestor=#{cid}")
	int selectCountByCategory(int cid);
}
