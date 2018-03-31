package net.kaciras.blog.domain.draft;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 草稿经常变动，就不缓存了
 */
@Mapper
interface DraftDAO {

	@Insert("INSERT INTO DraftUser(user_id,article_id) VALUES (#{userId},#{articleId})")
	@Options(useGeneratedKeys = true, keyColumn = "id")
	void insertAssoicate(Draft draft);

	@Insert("INSERT INTO Draft(id,save_count,title,cover,summary,keywords,content) " +
			"VALUES(#{id}, (SELECT IFNULL(sc, 0) FROM (SELECT MAX(save_count)+1 AS sc FROM Draft WHERE id=#{id}) AS Self)," +
			"#{content.title}, #{content.cover}, #{content.summary}, #{content.keywords}, #{content.content})")
	void insertHistory(@Param("id") int id, @Param("content") DraftContentBase content);

	@Delete("DELETE FROM Draft WHERE id=#{id} AND save_count=(SELECT sc FROM(SELECT MIN(save_count) AS sc FROM Draft) AS Self)")
	void deleteOldest(int id);

	@Select("SELECT COUNT(*) FROM DraftUser WHERE user_id=#{uid}")
	int selectCountByUser(int uid);

	@Select("SELECT COUNT(*) FROM Draft WHERE id=#{id}")
	int selectCountById(int id);

	@Delete({
			"DELETE FROM Draft WHERE id=#{id};",
			"DELETE FROM DraftUser WHERE id=#{id}"
	})
	int deleteById(int id);

	@Delete({
			"DELETE FROM DraftUser WHERE user_id=#{uid};",
			"DELETE FROM Draft WHERE id IN (SELECT id FROM DraftUser WHERE user_id=#{uid});"
	})
	void deleteAll(int uid);

	@Select("SELECT A.article_id,A.user_id,B.* FROM DraftUser AS A " +
			"JOIN Draft AS B ON A.id=B.id " +
			"WHERE A.id=#{id} ORDER BY save_count DESC LIMIT 1")
	@ResultMap("net.kaciras.blog.domain.dao.ResultMap.DraftMap")
	Draft selectById(int id);

	//连接 + 分组 + 排序太麻烦，直接上层处理
	@Select("SELECT id FROM DraftUser WHERE user_id=#{uid}")
	List<Integer> selectByUser(int uid);

	@Select("SELECT * FROM Draft WHERE id=#{id} ORDER BY save_count DESC")
	@ResultMap("net.kaciras.blog.domain.dao.ResultMap.DraftHistoryMap")
	List<DraftHistory> selectHistories(int id);
}
