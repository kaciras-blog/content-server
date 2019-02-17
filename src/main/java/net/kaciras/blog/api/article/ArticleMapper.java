package net.kaciras.blog.api.article;

import net.kaciras.blog.api.MapStructConfig;
import net.kaciras.blog.api.category.Category;
import net.kaciras.blog.api.category.CategoryManager;
import net.kaciras.blog.api.category.CategoryRepository;
import net.kaciras.blog.api.discuss.DiscussionService;
import net.kaciras.blog.api.user.UserManager;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Mapper(config = MapStructConfig.class)
abstract class ArticleMapper {

	@Autowired
	private DiscussionService discussionService;

	@Autowired
	private UserManager userManager;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private CategoryManager categoryManager;

	private final Pattern urlKeywords = Pattern.compile("[\\s?#@:&\\\\/=\"'`,.!]+");

	public ArticleVo toViewObject(Article article) {
		var vo = createVoFrom(article);
		vo.setNext(article.getNextLink());
		vo.setPrev(article.getPreviousLink());
		vo.setBanner(categoryManager.getBanner(article.getCategory()));
		return vo;
	}

	public List<PreviewVo> toPreview(List<Article> articles, ArticleListQuery request) {
		return articles.stream().map(article -> toPreview(article, request)).collect(Collectors.toList());
	}

	abstract ArticleVo createVoFrom(Article article);

	abstract List<SimpleCategoryVo> mapCategoryPath(List<Category> categories);


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
