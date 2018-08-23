package net.kaciras.blog.discuss;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface VoMapper {

	DiscussionVo discussionView(Discussion dto);
}
