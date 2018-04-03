package net.kaciras.blog.domain.user;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
interface BanRecordDao {

	@Insert("INSERT INTO BanRecord(user_id,start,end,operator,cause) " +
			"VALUES (#{uid},#{r.start},#{r.end},#{r.operator},#{r.cause})")
	void insertBanRecord(@Param("uid") int id, @Param("r") BanRecord record);

	@Select("SELECT MAX(end) FROM BanRecord WHERE user_id=#{uid}")
	LocalDateTime selectUnblockTime(int id);

	@Insert("INSERT UnBanRecord(ban_id,operator,cause) VALUES(#{bid}, #{op}, #{cause})")
	void insertUnbanRecord(@Param("bid") int bid,
						   @Param("op") int operator,
						   @Param("cause") String cause);

	@Select("SELECT * FROM BanRecord WHERE user_id=#{uid}")
	List<BanRecord> selectBanRecords(int uid);
}
