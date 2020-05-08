package com.kaciras.blog.api.accesslog;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
interface AccessLoggingDAO {

	@Insert("INSERT INTO access_log(ip, method, path, status, params, user_agent, `length`, `time`, delay)" +
			"VALUES(#{ip}, #{method}, #{path}, #{statusCode}, #{params}, #{userAgent}, #{length}, #{time}, #{delay})")
	void insert(AccessRecord record);
}
