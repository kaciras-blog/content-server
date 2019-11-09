package net.kaciras.blog.api.article;

import net.kaciras.blog.api.MapStructConfig;
import net.kaciras.blog.api.category.Category;
import net.kaciras.blog.api.category.CategoryManager;
import net.kaciras.blog.api.category.CategoryRepository;
import net.kaciras.blog.api.discuss.DiscussionQuery;
import net.kaciras.blog.api.discuss.DiscussionService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Mapper(config = MapStructConfig.class)
abstract class ArticleMapper {

	@Autowired
	private DiscussionService discussionService;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private CategoryManager categoryManager;

	private final Pattern urlKeywords = Pattern.compile("[\\s?#@:&\\\\/=\"'`,.!]+");

	public ArticleVo toViewObject(Article article) {
		var vo = createVoFrom(article);
		article.getPrev().map(ArticleLink::of).ifPresent(vo::setPrev);
		article.getNext().map(ArticleLink::of).ifPresent(vo::setNext);
		vo.setBanner(categoryManager.getBanner(article.getCategory()));
		return vo;
	}

	public List<PreviewVo> toPreview(@NonNull List<Article> articles, ArticleListQuery request) {
		return articles.stream().map(article -> toPreview(article, request)).collect(Collectors.toList());
	}

	/**
	 * 将用户信息，评论数，分类路径和文章聚合为一个对象，节约前端请求次数。
	 *
	 * @param article 文章对象
	 * @return 聚合后的对象
	 */
	PreviewVo toPreview(Article article, ArticleListQuery request) {
		var vo = createPreviewFrom(article);
		if (request.isContent()) {
			vo.setContent(article.getContent());
		}
		var categoryPath = categoryRepository.get(article.getCategory()).getPathTo(request.getCategory());
		vo.setCategories(mapCategoryPath(categoryPath));
		vo.setDiscussionCount(discussionService.count(new DiscussionQuery().setObjectId(article.getId()).setType(0)));
		return vo;
	}

	// 排除内容属性，由外层的方法决定是否复制
	@Mapping(target = "content", ignore = true)
	abstract PreviewVo createPreviewFrom(Article article);

	@Mapping(target = "prev", ignore = true)
	@Mapping(target = "next", ignore = true)
	abstract ArticleVo createVoFrom(Article article);

	abstract List<SimpleCategoryVo> mapCategoryPath(List<Category> categories);

	/**
	 * 由发表请求创建文章对象，是文章的工厂方法。
	 *
	 * @param request 发表请求
	 * @return 文章对象
	 */
	public Article createArticle(PublishInput request) {
		var article = new Article();
		update(article, request);

		article.setCategory(request.getCategory());
		article.setUrlTitle(StringUtils.trimTrailingCharacter(urlKeywords
				.matcher(request.getUrlTitle()).replaceAll("-"), '-'));
		return article;
	}

	public abstract void update(@MappingTarget Article article, ArticleContentBase contentBase);
}
