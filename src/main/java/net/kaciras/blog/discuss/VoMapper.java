package net.kaciras.blog.discuss;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VoMapper {

	DiscussionVo discussionView(Discussion dto);

}
