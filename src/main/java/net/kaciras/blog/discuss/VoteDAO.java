package net.kaciras.blog.discuss;

import org.apache.ibatis.annotations.*;

@Mapper
interface VoteDAO {

	@Insert("INSERT INTO DiscussionVote (id,user) VALUES (#{id}, #{userId})")
	void insertRecord(@Param("id") int id, @Param("userId") int userId);

	@Delete("DELETE FROM DiscussionVote WHERE id=#{id} AND user=#{userId}")
	int deleteRecord(@Param("id") int id, @Param("userId") int userId);

	@Select("SELECT 1 FROM DiscussionVote WHERE id=#{id} AND user=#{userId}")
	Boolean contains(@Param("id") int id, @Param("userId") int userId);
}
