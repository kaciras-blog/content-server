package net.kaciras.blog.api.discuss;

import org.apache.ibatis.annotations.*;

@Mapper
interface VoteDAO {

	@Insert("INSERT INTO discussion_vote (id,user) VALUES (#{id}, #{userId})")
	void insertRecord(long id, int userId);

	@Delete("DELETE FROM discussion_vote WHERE id=#{id} AND user=#{userId}")
	int deleteRecord(long id, int userId);

	@Select("SELECT 1 FROM discussion_vote WHERE id=#{id} AND user=#{userId}")
	Boolean contains(long id, int userId);

	// 下面两个是在discuss表中冗余点赞数，便于排序。在点赞后不要忘了更新。
	
	@Update("UPDATE discussion SET vote=vote+1 WHERE id=#{id}")
	void increaseVote(long id);

	@Update("UPDATE discussion SET vote=vote-1 WHERE id=#{id}")
	void decreaseVote(long id);
}
