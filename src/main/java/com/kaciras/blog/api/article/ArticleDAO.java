package com.kaciras.blog.api.article;

import org.apache.ibatis.annotations.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 这里不涉及分类字段，分类由ClassifyDAO操作。
 */
@Mapper
interface ArticleDAO {

	@Select("SELECT * FROM article WHERE id=#{id}")
	@ResultMap("com.kaciras.blog.api.article.ArticleDAO.ArticleMap")
	Optional<Article> selectById(int id);

	// 小心注入，comparator 参数必须限制在比较符号。
	@Select("SELECT * FROM article WHERE id ${comparator} #{id} AND deleted=0 LIMIT 1")
	@ResultMap("com.kaciras.blog.api.article.ArticleDAO.ArticleMap")
	Optional<Article> getNeighbor(int id, String comparator);

	@Select("SELECT COUNT(*) FROM article")
	int selectCount();

	/**
	 * 没指定分类的话单独搞一个方法查询性能好点
	 *
	 * @param query 查询请求对象
	 * @return 文章预览信息列表
	 */
	@SelectProvider(type = SqlProvider.class, method = "selectPreview")
	@ResultMap("com.kaciras.blog.api.article.ArticleDAO.ArticleMap")
	List<Article> selectPreview(ArticleListQuery query);

	@Insert("INSERT INTO article(category, title, url_title, cover, summary, content) " +
			"VALUES(#{category}, #{title}, #{urlTitle}, #{cover}, #{summary}, #{content})")
	@Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
	void insert(Article article);

	/**
	 * 更改一个文章的删除状态，并不会真正地从数据库中删除或插入它。
	 *
	 * @param id    文章ID。
	 * @param value 删除标记的值
	 */
	@Update("UPDATE article SET deleted=#{value} WHERE id=#{id}")
	void updateDeleted(int id, boolean value);

	@Update("UPDATE article SET url_title=#{urlTitle} WHERE id=#{id}")
	void updateUrlTitle(int id, String urlTitle);

	@Update("UPDATE article SET title=#{title},cover=#{cover},summary=#{summary}," +
			"content=#{content},update_time=NOW() WHERE id=#{id}")
	int update(Article article);

	@Update("UPDATE article SET view_count=view_count+1 WHERE id=#{id}")
	void increaseViewCount(int id);

	@Select("SELECT MAX(update_time) FROM article")
	Instant selectLastUpdateTime();
}
