package net.kaciras.blog.article;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
interface ArticleMapper {

	ArticlePreviewVo articlePreview(Article article);

	List<ArticlePreviewVo> toPreviewVo(List<Article> articles);

	ArticleVo articleView(Article article);

	Article publishToArticle(ArticlePublishDTO publishDTO);
}
