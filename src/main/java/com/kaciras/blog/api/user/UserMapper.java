package com.kaciras.blog.api.user;

import com.kaciras.blog.api.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
interface UserMapper {

	UserVO toUserVo(User user);

	void populate(@MappingTarget User user, UpdateDTO data);
}
