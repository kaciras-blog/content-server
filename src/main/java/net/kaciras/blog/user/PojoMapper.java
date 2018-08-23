package net.kaciras.blog.user;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface PojoMapper {

	UserVo toUserVo(User user);
}
