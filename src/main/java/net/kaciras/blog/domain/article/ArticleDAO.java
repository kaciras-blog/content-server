package net.kaciras.blog.domain.article;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 这里不涉及分类字段，分类由ClassifyDAO操作。
 */
@Mapper
public interface ArticleDAO {

	@Select("SELECT id,user_id,title,summary,cover,content,deleted,update_time,create_time,view_count " +
			"FROM Article WHERE id=#{id}")
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

	@Insert("INSERT INTO Article(user_id,title,cover,summary,content) " +
			"VALUES(#{userId},#{title},#{cover},#{summary},#{content})")
	@Options(useGeneratedKeys = true, keyColumn = "id")
	void insert(Article article);

	@Update("UPDATE Article SET deleted=#{value} WHERE id=#{id}")
	int updateDeleted(@Param("id") int id, @Param("value") boolean value);

	@Update("UPDATE Article SET " +
			"title=#{title}," +
			"cover=#{cover}," +
			"summary=#{summary}," +
			"content=#{content} " +
			"WHERE id=#{id}")
	int update(Article article);

	@Update("UPDATE Article SET view_count=view_count+1 WHERE id=#{id}")
	void increaseViewCount(int id);

	/**
	 * 这个是专门用来检查文章删除情况的
	 *
	 * @param id 文章id
	 * @return true：如果文章被删除，false：文章没有删除，null：文章不存在
	 */
	@Select("SELECT deleted FROM Article WHERE id=#{id}")
	Boolean selectDeletedById(int id);
}
