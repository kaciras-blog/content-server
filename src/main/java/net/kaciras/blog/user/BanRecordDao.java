package net.kaciras.blog.user;

import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
interface BanRecordDao {

	@Insert("INSERT INTO BanRecord(user_id,start,end,operator,cause) " +
			"VALUES (#{uid},#{r.start},#{r.end},#{r.operator},#{r.cause})")
	@Options(useGeneratedKeys = true, keyColumn = "id")
	void insertBanRecord(@Param("uid") int id, @Param("r") BanRecord record);

	@Select("SELECT MAX(A.end) FROM BanRecord AS A " +
			"LEFT JOIN UnbanRecord AS B ON A.id=B.id " +
			"WHERE A.user_id=#{uid} AND A.end>NOW() AND B.id IS NULL")
	LocalDateTime selectLastEndTime(int id);

	@Insert("INSERT INTO UnBanRecord(id,operator,cause) VALUES(#{bid}, #{op}, #{cause})")
	void insertUnbanRecord(@Param("bid") int bid,
						   @Param("op") int operator,
						   @Param("cause") String cause);

	@Select("SELECT * FROM BanRecord AS A WHERE user_id=#{uid}")
	@Results({
			@Result(property = "id", column = "id"),
			@Result(property = "unbanRecord", column = "id", one = @One(select = "net.kaciras.blog.domain.user.BanRecordDao.selectBanRecords"))
	})
	List<BanRecord> selectBanRecords(int uid);

	@Select("SELECT * FROM UnbanRecord WHERE id=#{banId}")
	UnbanRecord selectUnbanRecord(int banId);
}
