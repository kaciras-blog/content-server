package net.kaciras.blog.api.discuss;

import org.apache.ibatis.annotations.*;

@Mapper
interface VoteDAO {

	@Insert("INSERT INTO discussion_vote (id,user) VALUES (#{id}, #{userId})")
	void insertRecord(@Param("id") int id, @Param("userId") int userId);

	@Delete("DELETE FROM discussion_vote WHERE id=#{id} AND user=#{userId}")
	int deleteRecord(@Param("id") int id, @Param("userId") int userId);

	@Select("SELECT 1 FROM discussion_vote WHERE id=#{id} AND user=#{userId}")
	Boolean contains(@Param("id") int id, @Param("userId") int userId);
}
