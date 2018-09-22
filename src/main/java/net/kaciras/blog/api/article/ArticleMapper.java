package net.kaciras.blog.api.article;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
interface ArticleMapper {

	List<PreviewVo> toPreview(List<Article> article);

	@Mapping(target = "vcnt", source = "viewCount")
	PreviewVo toPreview(Article article);

	ArticleVo toViewObject(Article article);

	Article toArticle(ArticleContentBase contentBase);

	void update(ArticleContentBase contentBase, @MappingTarget Article article);
}
