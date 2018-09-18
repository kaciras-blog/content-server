package net.kaciras.blog.api.draft;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 草稿经常变动，就不缓存了
 */
@Mapper
interface DraftDAO {

	@Insert("INSERT INTO draft_user(user_id,article_id) VALUES (#{userId},#{articleId})")
	@Options(useGeneratedKeys = true, keyColumn = "id")
	void insertAssoicate(Draft draft);

	@Insert("INSERT INTO Draft(id,save_count,title,cover,summary,keywords,content) " +
			"VALUES(#{id}, (SELECT IFNULL(sc, 0) FROM (SELECT MAX(save_count)+1 AS sc FROM Draft WHERE id=#{id}) AS Self)," +
			"#{content.title}, #{content.cover}, #{content.summary}, #{content.keywords}, #{content.content})")
	void insertHistory(@Param("id") int id, @Param("content") DraftContentBase content);

	@Delete("DELETE FROM Draft WHERE id=#{id} AND save_count=(SELECT sc FROM(SELECT MIN(save_count) AS sc FROM Draft) AS Self)")
	void deleteOldest(int id);

	@Update("UPDATE Draft SET title=#{content.title}," +
			"cover=#{content.cover}," +
			"summary=#{content.summary}," +
			"keywords=#{content.keywords}," +
			"content=#{content.content} " +
			"WHERE id=#{draft.id} AND save_count=#{draft.saveCount}")
	int update(@Param("draft") Draft draft, @Param("content") DraftContentBase content);

	@Select("SELECT COUNT(*) FROM draft_user WHERE user_id=#{uid}")
	int selectCountByUser(int uid);

	@Select("SELECT COUNT(*) FROM Draft WHERE id=#{id}")
	int selectCountById(int id);

	/**
	 * 删除指定id的草稿，包括其所有的历史记录
	 *
	 * @param id 草稿id
	 * @return 删除的行数
	 */
	@Delete({
			"DELETE FROM Draft WHERE id=#{id};",
			"DELETE FROM draft_user WHERE id=#{id}"
	})
	int deleteById(int id);

	/**
	 * 删除指定用户所有的草稿。
	 *
	 * @param uid 用户id
	 */
	@Delete({
			"DELETE FROM draft_user WHERE user_id=#{uid};",
			"DELETE FROM Draft WHERE id IN (SELECT id FROM draft_user WHERE user_id=#{uid});"
	})
	void deleteAll(int uid);

	@Select("SELECT A.article_id,A.user_id,B.* FROM draft_user AS A " +
			"JOIN Draft AS B ON A.id=B.id " +
			"WHERE A.id=#{id} ORDER BY save_count DESC LIMIT 1")
	@ResultMap("net.kaciras.blog.domain.dao.ResultMap.DraftMap")
	Draft selectById(int id);

	//连接 + 分组 + 排序太麻烦，直接上层处理
	@Select("SELECT id FROM draft_user WHERE user_id=#{uid}")
	List<Integer> selectByUser(int uid);

	@Select("SELECT * FROM Draft WHERE id=#{id} ORDER BY save_count DESC")
	@ResultMap("net.kaciras.blog.domain.dao.ResultMap.DraftHistoryMap")
	List<DraftHistory> selectHistories(int id);
}
