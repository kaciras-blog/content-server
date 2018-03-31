package net.kaciras.blog.domain.accesslog;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccessLogDAO {

	@Insert("INSERT INTO AccessLog(address,path,status,referer,browser,browser_version,system)" +
			"VALUES(#{address},#{path},#{statusCode},#{referer},#{browser},#{browserVersion},#{system})")
	void insert(AccessRecord record);
}
