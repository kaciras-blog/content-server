package net.kaciras.blog.article;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ClassifyDAO {

	@Update("UPDATE Article SET category=#{cid} WHERE id=#{aid}")
	int updateByArticle(@Param("aid") int aid, @Param("cid") int cid);

	@Update("UPDATE Article SET category=#{New} WHERE category=#{old}")
	void updateCategory(@Param("old") int old, @Param("New") int New);

	@Select("SELECT category FROM Article WHERE id=#{id}")
	List<Integer> selectById(int id);
//
//	@Select("SELECT A.id,A.category,A.title,A.cover,A.summary,A.keywords,A.update_time,A.create_time,A.view_count " +
//			"FROM Article AS A JOIN CategoryTree AS B ON A.category=B.descendant WHERE B.ancestor=#{cid} AND A.deleted=0")
//	@ResultMap("net.kaciras.blog.domain.dao.ResultMap.ArticleMap")
//	List<Article> selectByAncestor(int cid);

	@Deprecated
	@Select("SELECT COUNT(*) FROM Article AS A JOIN CategoryTree AS B ON A.category=B.descendant WHERE deleted=0 AND B.ancestor=#{cid}")
	int selectCountByCategory(@Param("cid") int cid);

	@SelectProvider(type = ArticleSqlProvider.class, method = "countInCategories")
	int selectCountByCategory2(@Param("arg0") List<Integer> ids);
}
