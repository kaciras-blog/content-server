package com.kaciras.blog.api.delaylog;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
interface DelayLoggingDAO {

	@Insert("INSERT INTO delay_log(ip, path, status, `length`, user_agent, `time`, delay)" +
			"VALUES(#{ip}, #{path}, #{statusCode}, #{length}, #{userAgent}, #{time}, #{delay})")
	void insert(DelayRecord record);
}
