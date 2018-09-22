package net.kaciras.blog.api.article;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
interface ArticleMapper {

	@Mapping(target = "vcnt", source = "viewCount")
	PreviewVo toPreview(Article article);

	ArticleVo toViewObject(Article article);

	Article toArticle(ArticleContentBase contentBase);

	void update(ArticleContentBase contentBase, @MappingTarget Article article);
}
