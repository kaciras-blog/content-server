package net.kaciras.blog.api.article;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 这里不涉及分类字段，分类由ClassifyDAO操作。
 */
@Mapper
interface ArticleDAO {

	@Select("SELECT id,user_id,title,summary,cover,content,deleted,update_time,create_time,view_count " +
			"FROM article WHERE id=#{id}")
	@ResultMap("net.kaciras.blog.domain.dao.ResultMap.ArticleMap")
	Article selectById(int id);

	/**
	 * 没指定分类的话单独搞一个方法查询性能好点
	 *
	 * @param query 查询请求对象
	 * @return 文章预览信息列表
	 */
	@SelectProvider(type = ArticleSqlProvider.class, method = "selectPreview")
	@ResultMap("net.kaciras.blog.domain.dao.ResultMap.ArticleMap")
	List<Article> selectPreview(ArticleListRequest query);

	@Insert("INSERT INTO article(user_id,title,cover,summary,content) " +
			"VALUES(#{userId},#{title},#{cover},#{summary},#{content})")
	@Options(useGeneratedKeys = true, keyColumn = "id")
	void insert(Article article);

	/**
	 * 更改一个文章的删除状态，并不会真正地从数据库中删除或插入它。
	 *
	 * @param id 文章ID。
	 * @param value 删除标记的值
	 */
	@Update("UPDATE article SET deleted=#{value} WHERE id=#{id}")
	void updateDeleted(@Param("id") int id, @Param("value") boolean value);

	@Update("UPDATE article SET title=#{title},cover=#{cover},summary=#{summary}," +
			"content=#{content} WHERE id=#{id}")
	int update(Article article);

	@Update("UPDATE article SET view_count=view_count+1 WHERE id=#{id}")
	void increaseViewCount(int id);

	@Select("SELECT id,title FROM article WHERE id ${compartor} #{id} LIMIT 1")
	@ResultMap("net.kaciras.blog.domain.dao.ResultMap.ArticleLinkMap")
	ArticleLink getNeighbor(@Param("id") int id, @Param("compartor") String compartor);
}
