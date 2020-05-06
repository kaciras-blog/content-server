package com.kaciras.blog.api.user;

import com.kaciras.blog.api.MapStructConfig;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
interface UserMapper {

	UserVo toUserVo(User user);
}