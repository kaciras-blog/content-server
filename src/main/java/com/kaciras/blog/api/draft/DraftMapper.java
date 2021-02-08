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
	List<DraftVO> toDraftVo(List<Draft> draft);

	@Named("toDraftVo")
	default DraftVO toDraftVo(Draft draft) {
		var vo = createDraftVo(draft);
		var lastHistory = draft.getHistoryList().findLatest();
		vo.title = lastHistory.getTitle();
		vo.updateTime = lastHistory.getTime();
		vo.lastSaveCount = lastHistory.getSaveCount();
		return vo;
	}

	@Mapping(target = "createTime", source = "time")
	DraftVO createDraftVo(Draft draft);
}
