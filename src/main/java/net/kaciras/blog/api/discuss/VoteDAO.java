package net.kaciras.blog.api.discuss;

import org.apache.ibatis.annotations.*;

import java.net.InetAddress;

@Mapper
interface VoteDAO {

	@Insert("INSERT INTO discussion_vote (id, address) VALUES (#{id}, #{address})")
	void insertRecord(int id, InetAddress address);

	@Delete("DELETE FROM discussion_vote WHERE id=#{id} AND address=#{address}")
	int deleteRecord(int id, InetAddress address);

	@Select("SELECT 1 FROM discussion_vote WHERE id=#{id} AND address=#{address}")
	Boolean contains(int id, InetAddress address);

	// 下面两个是在discuss表中冗余点赞数，便于排序。在点赞后不要忘了更新。
	
	@Update("UPDATE discussion SET vote=vote+1 WHERE id=#{id}")
	void increaseVote(int id);

	@Update("UPDATE discussion SET vote=vote-1 WHERE id=#{id}")
	void decreaseVote(int id);
}
