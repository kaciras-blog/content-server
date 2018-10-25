package net.kaciras.blog.api.article;

import net.kaciras.blog.api.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
interface ArticleMapper {

	@Mapping(target = "vcnt", source = "viewCount")
	PreviewVo toPreview(Article article);

	ArticleVo toViewObject(Article article);

	Article toArticle(ArticleContentBase contentBase);

	void update(@MappingTarget Article article, ArticleContentBase contentBase);
}
