package com.kaciras.blog.api.account.oauth2;

import com.kaciras.blog.api.account.AuthType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
interface OAuth2DAO {

	@Insert("INSERT INTO oauth(oauth_id, type, local_id) VALUES (#{oauthId}, #{type}, #{localId})")
	void insert(String oauthId, AuthType type, int localId);

	// Mybatis 仍不支持 OptionalInt
	@Select("SELECT local_id FROM oauth WHERE oauth_id=#{oauthId} AND type=#{type}")
	Integer select(String oauthId, AuthType type);
}
