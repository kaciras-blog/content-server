package net.kaciras.blog.api.discuss;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
interface DiscussionDAO {

	@Insert("INSERT INTO discussion(user_id, object_id, type, floor, parent, content, state, address) " +
			"VALUES (#{userId}, #{objectId}, #{type}, #{floor}, #{parent}, #{content}, #{state}, #{address})")
	@Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
	void insert(Discussion discussion);

	@SelectProvider(type = SqlProvider.class, method = "select")
	@ResultMap("net.kaciras.blog.api.discuss.DiscussionDAO.DiscussionMap")
	List<Discussion> selectList(DiscussionQuery query);

	@SelectProvider(type = SqlProvider.class, method = "selectCount")
	int selectCount(DiscussionQuery query);

	@Select("SELECT * FROM discussion WHERE id=#{id}")
	@ResultMap("net.kaciras.blog.api.discuss.DiscussionDAO.DiscussionMap")
	Optional<Discussion> selectById(long id);

	@Update("UPDATE discussion SET state=#{state} WHERE id=#{id}")
	int updateState(long id, DiscussionState state);

	/**
	 * 查询指定对象的评论数量，不包含评论的回复（楼中楼）。
	 *
	 * @param objectId 对象ID
	 * @return 评论数，不含楼中楼
	 */
	@Select("SELECT COUNT(*) FROM discussion WHERE object_id=#{objectId} AND type=#{type} AND parent=0")
	int selectCountByObject(int objectId, int type);
}
