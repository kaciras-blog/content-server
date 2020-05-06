package com.kaciras.blog.api.draft;

import com.kaciras.blog.api.MapStructConfig;
import com.kaciras.blog.api.article.Article;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(config = MapStructConfig.class)
interface DraftMapper {

	@Mapping(target = "keywords", expression = "java(String.join(\" \", article.getKeywords()))")
	DraftContent fromArticle(Article article);

	@IterableMapping(qualifiedByName = "toDraftVo")
	List<DraftVo> toDraftVo(List<Draft> draft);

	@Named("toDraftVo")
	default DraftVo toDraftVo(Draft draft) {
		var vo = createDraftVo(draft);
		var lastHistory = draft.getHistoryList().findLatest();
		vo.setTitle(lastHistory.getTitle());
		vo.setUpdateTime(lastHistory.getTime());
		vo.setLastSaveCount(lastHistory.getSaveCount());
		return vo;
	}

	@Mapping(target = "createTime", source = "time")
	DraftVo createDraftVo(Draft draft);
}
