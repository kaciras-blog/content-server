package net.kaciras.blog.article;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
interface ArticleMapper {

	List<PreviewVo> toPreview(List<Article> article);

	@Mapping(target = "vcnt", source = "viewCount")
	PreviewVo toPreview(Article article);

	ArticleVo toViewObject(Article article);

	Article publishToArticle(ArticlePublishDTO publishDTO);
}
