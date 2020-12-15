package com.kaciras.blog.api.discuss;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
interface DiscussionDAO {

	@InsertProvider(type = SqlProvider.class, method = "insert")
	@Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
	void insert(Discussion discussion);

	@SelectProvider(type = SqlProvider.class, method = "select")
	List<Discussion> selectList(DiscussionQuery query);

	@Select("SELECT * FROM discussion WHERE id=#{id}")
	Optional<Discussion> selectById(int id);

	/**
	 * 获取符合查询条件的评论总数，分页属性将被忽略。
	 *
	 * @param query 查询对象
	 * @return 评论数
	 */
	@SelectProvider(type = SqlProvider.class, method = "selectCount")
	int count(DiscussionQuery query);

	/**
	 * 修改评论的回复数，用于子评论改动后确保父评论数据一致。
	 *
	 * @param id    评论ID
	 * @param value 增量
	 */
	@Update("UPDATE discussion SET reply_count = reply_count + #{value} WHERE id=#{id}")
	void addReplyCount(int id, int value);

	/**
	 * 更新一条评论的状态。
	 *
	 * @param id    评论ID
	 * @param state 新状态
	 */
	@Update("UPDATE discussion SET state=#{state} WHERE id=#{id}")
	void updateState(int id, DiscussionState state);

	/**
	 * 查询指定的评论有多少个子评论，比用 DiscussionQuery 简洁些。
	 *
	 * @param id 评论ID
	 * @return 子评论的数量
	 */
	@Select("SELECT COUNT(*) FROM discussion WHERE parent=#{id}")
	int countByParent(int id);

	/**
	 * 查询评论所在频道的顶层评论数量，不包含子评论（楼中楼）。
	 *
	 * @param discussion 评论
	 * @return 评论数，不含楼中楼
	 */
	@Select("SELECT COUNT(*) FROM discussion WHERE object_id=#{objectId} AND type=#{type} AND parent=0")
	int countTopLevel(Discussion discussion);
}
