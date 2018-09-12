package net.kaciras.blog.api.draft;


import net.kaciras.blog.api.article.Article;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
interface DraftMapper {

	DraftDTO toDTO(Draft draft);

	List<DraftDTO> toDTOList(List<Draft> drafts);

	@Mapping(target = "articleId", source = "id")
	@Mapping(target = "keywords", expression = "java(String.join(\" \", article.getKeywords()))")
	Draft fromArticle(Article article);

	DraftVo draftView(DraftDTO draft);

	@Named("DraftHistory")
	DraftHistoryVo toDraftHistoryVO(DraftHistory draft);

	@IterableMapping(qualifiedByName = "DraftHistory")
	List<DraftHistoryVo> toDraftHistoryVOList(List<DraftHistory> histories);

	@Named("Draft")
	DraftPreviewVo toDraftPreviewVO(DraftDTO draft);

	@IterableMapping(qualifiedByName = "Draft")
	List<DraftPreviewVo> toDraftPreviewVOList(List<DraftDTO> histories);
}
