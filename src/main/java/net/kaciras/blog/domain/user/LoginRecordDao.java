package net.kaciras.blog.domain.user;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.net.InetAddress;
import java.util.List;

@Mapper
interface LoginRecordDao {

	@Insert("INSERT INTO LoginRecord(user,address) VALUES(#{user}, #{address})")
	void insert(@Param("user") int userId, @Param("address") InetAddress address);

	@Select("SELECT address,time FROM LoginRecord WHERE user=#{userId} ORDER BY time DESC ")
	List<LoginRecord> select(int userId);

	@Select("SELECT address,time FROM LoginRecord WHERE user=#{userId} ORDER BY time DESC LIMIT 1")
	LoginRecord selectMax(int userId);
}
