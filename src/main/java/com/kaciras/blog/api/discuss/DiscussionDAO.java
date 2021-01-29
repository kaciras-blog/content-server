package com.kaciras.blog.api.discuss;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * 每个评论都有一个可选的父评论，以及任意数量的子评论，在数据结构上表现为树。
 * 前端的两种模式：楼中楼和引用，也都能用树来表示。
 * <p>
 * 不同于分类，评论没有跨级查询需求，故没必要使用闭包表，直接存个 parent 即可。
 */
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
	@Update("UPDATE discussion SET nest_size = nest_size + #{value} WHERE id=#{id}")
	void addNestSize(int id, int value);

	/**
	 * 更新一条评论的状态。
	 *
	 * @param id    评论ID
	 * @param state 新状态
	 */
	@Update("UPDATE discussion SET state=#{state} WHERE id=#{id}")
	void updateState(int id, DiscussionState state);

	@Select("SELECT COUNT(*) FROM discussion WHERE type=#{type} AND object_id=#{objectId}")
	int countByTopic(Discussion discussion);

	/**
	 * 查询评论所在的楼中楼有多少个评论，比用 DiscussionQuery 简洁些。
	 *
	 * @param discussion 评论
	 * @return 子评论的数量
	 */
	@Select("SELECT COUNT(*) FROM discussion WHERE nest_id=#{nestId}")
	int countByNest(Discussion discussion);

	/**
	 * 查询评论所在主题的顶层评论数量，不包含子评论（楼中楼）。
	 *
	 * @param discussion 评论
	 * @return 评论数，不含楼中楼
	 */
	@Select("SELECT COUNT(*) FROM discussion WHERE type=#{type} AND object_id=#{objectId} AND parent=0")
	int countTopLevel(Discussion discussion);
}
