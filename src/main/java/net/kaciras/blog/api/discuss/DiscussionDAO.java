package net.kaciras.blog.api.discuss;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
interface DiscussionDAO {

	@Insert("INSERT INTO discussion(user,post,floor,content) " +
			"VALUES (#{userId},#{articleId},#{floor},#{content})")
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
	int updateDeleted(@Param("id") int id, @Param("value") boolean value);

	@Select("SELECT IFNULL(MAX(floor), 0) FROM discussion WHERE post=#{postId}")
	int selectLastFloor(int postId);

	/**
	 * 在discuss表中冗余点赞数，便于排序。在点赞后不要忘了更新。
	 *
	 * @param id 评论id
	 */
	@Update("UPDATE discussion SET vote=vote+1 WHERE id=#{id}")
	void increaseVote(int id);

	@Update("UPDATE discussion SET vote=vote-1 WHERE id=#{id}")
	void descreaseVote(int id);
}
