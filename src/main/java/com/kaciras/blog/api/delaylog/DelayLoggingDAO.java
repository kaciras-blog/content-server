package com.kaciras.blog.api.delaylog;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
interface DelayLoggingDAO {

	@Insert("INSERT INTO delay_log(ip, path, params, status, `length`, `time`, delay)" +
			"VALUES(#{ip}, #{path}, #{params}, #{status}, #{length}, #{time}, #{delay})")
	void insert(DelayRecord record);
}
