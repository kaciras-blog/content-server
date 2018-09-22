package net.kaciras.blog.api.user;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface UserMapper {

	UserVo toUserVo(User user);
}
