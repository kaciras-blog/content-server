package net.kaciras.blog.api.draft;


import net.kaciras.blog.api.article.Article;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
interface DraftMapper {

	@Mapping(target = "articleId", source = "id")
	@Mapping(target = "keywords", expression = "java(String.join(\" \", article.getKeywords()))")
	Draft fromArticle(Article article);

	DraftVo toVo(Draft draft);

	List<DraftVo> toVo(List<Draft> drafts);

	@IterableMapping(qualifiedByName = "DraftHistory")
	List<DraftHistoryVo> toDraftHistoryVOList(List<DraftHistory> histories);

	@Named("PreviewVo")
	DraftPreviewVo toPreviewVo(Draft draft);

	@IterableMapping(qualifiedByName = "PreviewVo")
	List<DraftPreviewVo> toPreviewVo(List<Draft> histories);
}
