package net.kaciras.blog.api.draft;

import net.kaciras.blog.api.article.Article;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
interface DraftMapper {

	@Mapping(target = "keywords", expression = "java(String.join(\" \", article.getKeywords()))")
	DraftContent fromArticle(Article article);

	DraftVo toVo(Draft draft);

	List<DraftVo> toVo(List<Draft> drafts);
}
