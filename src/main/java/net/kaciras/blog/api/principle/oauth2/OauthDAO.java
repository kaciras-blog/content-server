package net.kaciras.blog.api.principle.oauth2;

import net.kaciras.blog.api.principle.AuthType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.OptionalInt;

@Mapper
interface OauthDAO {

	@Insert("INSERT INTO oauth(oauth_id, type, local_id) VALUES (${oauthId}, ${type}, ${localId})")
	void insert(long oauthId, AuthType type, int localId);

	@Select("SELECT local_id FROM oauth WHERE oauth_id=${oauthId} AND type=${type}")
	OptionalInt select(long oauthId, AuthType type);
}
