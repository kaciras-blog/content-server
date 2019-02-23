package net.kaciras.blog.api.article;

import net.kaciras.blog.api.MapStructConfig;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

@Mapper(config = MapStructConfig.class)
abstract class ArticleMapper {

	private final Pattern urlKeywords = Pattern.compile("[\\s?#@:&\\\\/=\"'`,.!]+");

	@IterableMapping(qualifiedByName = "toViewObject")
	public abstract List<ArticleVo> toViewObject(List<Article> articles);

	@Named("toViewObject")
	public ArticleVo toViewObject(Article article) {
		var vo = createVoFrom(article);
		vo.setNext(article.getNextLink());
		vo.setPrev(article.getPreviousLink());
		return vo;
	}

	abstract ArticleVo createVoFrom(Article article);

	/**
	 * 由发表请求创建文章对象，是文章的工厂方法。
	 *
	 * @param request 发表请求
	 * @param userId  当前用户ID
	 * @return 文章对象
	 */
	public Article createArticle(PublishRequest request, int userId) {
		var article = new Article();
		update(article, request);
		article.setUserId(userId);

		article.setUrlTitle(StringUtils.trimTrailingCharacter(urlKeywords
				.matcher(request.getUrlTitle()).replaceAll("-"), '-'));
		return article;
	}

	public abstract void update(@MappingTarget Article article, ArticleContentBase contentBase);
}
