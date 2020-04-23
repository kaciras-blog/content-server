package net.kaciras.blog.api.draft;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 草稿经常变动，就不缓存了
 */
@Mapper
interface DraftDAO {

	@Select("SELECT * FROM draft WHERE id=#{id}")
	@ResultMap("net.kaciras.blog.api.draft.DraftDAO.DraftMap")
	Optional<Draft> selectById(int id);

	@Select("SELECT * FROM draft WHERE user_id=#{uid}")
	@ResultMap("net.kaciras.blog.api.draft.DraftDAO.DraftMap")
	List<Draft> selectByUser(int uid);

	@Insert("INSERT INTO draft(user_id, article_id) VALUES (#{userId}, #{articleId})")
	@Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
	void insert(Draft draft);

	@Select("SELECT COUNT(*) FROM draft WHERE user_id=#{uid}")
	int selectCountByUser(int uid);

	/**
	 * 删除指定id的草稿，包括其所有的历史记录
	 *
	 * @param id 草稿id
	 * @return 删除的行数
	 */
	@Delete({
			"DELETE FROM draft WHERE id=#{id};",
			"DELETE FROM draft_history WHERE id=#{id};",
	})
	int deleteById(int id);

	/**
	 * 删除指定用户所有的草稿。
	 *
	 * @param uid 用户id
	 */
	@Delete({
			"DELETE FROM draft WHERE user_id=#{uid};",
			"DELETE FROM draft_history WHERE id IN (SELECT id FROM draft WHERE user_id=#{uid});"
	})
	void deleteAll(int uid);
}
