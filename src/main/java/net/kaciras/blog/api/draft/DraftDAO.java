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
	void insert(Draft draft);

	@Select("SELECT COUNT(*) FROM draft_user WHERE user_id=#{uid}")
	int selectCountByUser(int uid);

	/**
	 * 删除指定id的草稿，包括其所有的历史记录
	 *
	 * @param id 草稿id
	 * @return 删除的行数
	 */
	@Delete({
			"DELETE FROM draft WHERE id=#{id};",
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
			"DELETE FROM draft WHERE id IN (SELECT id FROM draft_user WHERE user_id=#{uid});"
	})
	void deleteAll(int uid);

	/**
	 * 查出最新的草稿。
	 *
	 * @param id 草稿ID
	 * @return 草稿对象
	 */
	Draft selectById(int id);

	//连接 + 分组 + 排序太麻烦，直接上层处理
	@Select("SELECT id FROM draft_user WHERE user_id=#{uid}")
	List<Integer> selectByUser(int uid);
}
