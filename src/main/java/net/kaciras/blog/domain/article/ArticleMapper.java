package net.kaciras.blog.domain.article;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

	Article publishToArticle(ArticlePublishDTO publishDTO);

	ArticleDTO toDTO(Article article);

	List<ArticleDTO> toDTOList(List<Article> articles);
}
