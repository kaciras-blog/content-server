package net.kaciras.blog.api.accesslog;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
interface AccessLoggingDAO {

	@Insert("INSERT INTO access_log(ip, path, status, user_agent, start, delay)" +
			"VALUES(#{ip}, #{path}, #{statusCode}, #{userAgent}, #{startTime}, #{delay})")
	void insert(AccessRecord record);
}
