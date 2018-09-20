package net.kaciras.blog.api.discuss;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
interface DiscussionDAO {

	@Insert("INSERT INTO discussion(user_id, object_id, `type`, floor, content) " +
			"VALUES (#{userId}, #{objectId}, #{type}, #{floor}, #{content})")
	@Options(useGeneratedKeys = true, keyColumn = "id")
	void insert(Discussion discuz);

	@SelectProvider(type = SqlProvidor.class, method = "select")
	@ResultMap("net.kaciras.blog.domain.dao.ResultMap.DiscussionMap")
	List<Discussion> selectList(DiscussionQuery query);

	@SelectProvider(type = SqlProvidor.class, method = "selectCount")
	int selectCount(DiscussionQuery query);

	@Select("SELECT * FROM discussion WHERE id=#{id}")
	@ResultMap("net.kaciras.blog.domain.dao.ResultMap.DiscussionMap")
	Discussion selectById(int id);

	@Update("UPDATE discussion SET deleted=#{value} WHERE id=#{id}")
	int updateDeleted(@Param("id") long id, @Param("value") boolean value);

	/**
	 * 查找指定对象的评论数量，不包含评论的回复（楼中楼）。
	 *
	 * @param oid 对象ID
	 * @param type 对象类型
	 * @return 评论数
	 */
	@Select("SELECT COUNT(*) FROM discussion " +
			"WHERE object_id=#{arg0} AND `type`=#{arg1} AND parent=0")
	int selectCountByObject(int oid, int type);

	/**
	 * 在discuss表中冗余点赞数，便于排序。在点赞后不要忘了更新。
	 *
	 * @param id 评论id
	 */
	@Update("UPDATE discussion SET vote=vote+1 WHERE id=#{id}")
	void increaseVote(long id);

	@Update("UPDATE discussion SET vote=vote-1 WHERE id=#{id}")
	void descreaseVote(long id);
}
