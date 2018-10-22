package net.kaciras.blog.api.user;

import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
interface BanRecordDAO {

	@Insert("INSERT INTO ban_record(user_id,start,end,operator,cause) " +
			"VALUES (#{uid},#{record.start},#{record.end},#{record.operator},#{record.cause})")
	@Options(useGeneratedKeys = true, keyColumn = "id")
	void insertBanRecord(int uid, BanRecord record);

	@Select("SELECT MAX(A.end) FROM ban_record AS A " +
			"LEFT JOIN unban_record AS B ON A.id=B.id " +
			"WHERE A.user_id=#{uid} AND A.end>NOW() AND B.id IS NULL")
	LocalDateTime selectLastEndTime(int uid);

	@Insert("INSERT INTO unban_record(id,operator,cause) VALUES(#{bid}, #{operator}, #{cause})")
	void insertUnbanRecord(int bid, int operator, String cause);

	@Select("SELECT * FROM ban_record AS A WHERE user_id=#{uid}")
	@Results({
			@Result(property = "id", column = "id"),
			@Result(property = "unbanRecord", column = "id", one = @One(select = "net.kaciras.blog.api.user.BanRecordDAO.selectBanRecords"))
	})
	List<BanRecord> selectBanRecords(int uid);

	@Select("SELECT * FROM unban_record WHERE id=#{banId}")
	UnbanRecord selectUnbanRecord(int banId);
}
