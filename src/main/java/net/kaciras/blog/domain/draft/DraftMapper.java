package net.kaciras.blog.domain.draft;


import net.kaciras.blog.domain.article.Article;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DraftMapper {

	DraftDTO toDTO(Draft draft);

	List<DraftDTO> toDTOList(List<Draft> drafts);

	@Mapping(target = "articleId", source = "id")
	@Mapping(target = "keywords", expression = "java(String.join(\" \", article.getKeywords()))")
	Draft fromArticle(Article article);
}
