package net.kaciras.blog.api.discuss;

import org.apache.ibatis.annotations.*;

@Mapper
interface VoteDAO {

	@Insert("INSERT INTO discussion_vote (id,user) VALUES (#{id}, #{userId})")
	void insertRecord(@Param("id") long id, @Param("userId") int userId);

	@Delete("DELETE FROM discussion_vote WHERE id=#{id} AND user=#{userId}")
	int deleteRecord(@Param("id") long id, @Param("userId") int userId);

	@Select("SELECT 1 FROM discussion_vote WHERE id=#{id} AND user=#{userId}")
	Boolean contains(@Param("id") long id, @Param("userId") int userId);

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
