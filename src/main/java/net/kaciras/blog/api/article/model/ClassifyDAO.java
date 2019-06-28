package net.kaciras.blog.api.article.model;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
interface ClassifyDAO {

	@Update("UPDATE article SET category=#{cid} WHERE id=#{aid}")
	void updateByArticle(int aid, int cid);

	@Update("UPDATE article SET category=#{New} WHERE category=#{old}")
	void updateCategory(int old, int New);

	@SelectProvider(type = SqlProvider.class, method = "selectCount")
	int selectCount(ArticleListQuery query);
}
