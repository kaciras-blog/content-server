package net.kaciras.blog.user;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PojoMapper {





	UserVo toUserVo(User user);
}
