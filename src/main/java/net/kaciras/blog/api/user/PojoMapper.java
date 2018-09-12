package net.kaciras.blog.api.user;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface PojoMapper {

	UserVo toUserVo(User user);
}
