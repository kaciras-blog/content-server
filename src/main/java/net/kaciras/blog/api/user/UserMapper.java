package net.kaciras.blog.api.user;

import net.kaciras.blog.api.MapStructConfig;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
interface UserMapper {

	UserVo toUserVo(User user);
}
