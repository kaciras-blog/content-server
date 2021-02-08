package com.kaciras.blog.api.article;

import com.kaciras.blog.api.MapStructConfig;
import com.kaciras.blog.api.category.Banner;
import com.kaciras.blog.api.category.Category;
import com.kaciras.blog.api.category.CategoryManager;
import com.kaciras.blog.api.category.CategoryRepository;
import com.kaciras.blog.api.discuss.DiscussionQuery;
import com.kaciras.blog.api.discuss.DiscussionRepository;
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
	private DiscussionRepository discussionRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private CategoryManager categoryManager;

	private final Pattern urlKeywords = Pattern.compile("[\\s?#@:&\\\\/=\"'`,.!]+");

	/**
	 * 将内部的领域对象转换为面向前端的视图对象。
	 *
	 * @param article 文章
	 * @return 视图对象
	 */
	@Mapping(target = "banner", source = "article")
	public abstract ArticleVO toViewObject(Article article);

	abstract ArticleLink toLink(Article article);

	final Banner getBannerFrom(Article article) {
		return categoryManager.getBanner(article.getCategory());
	}

	public final List<PreviewVO> toPreview(@NonNull List<Article> articles, ArticleListQuery request) {
		return articles.stream().map(article -> toPreview(article, request)).collect(Collectors.toList());
	}

	/**
	 * 将用户信息，评论数，分类路径和文章聚合为一个对象，节约前端请求次数。
	 *
	 * @param article 文章对象
	 * @return 聚合后的对象
	 */
	final PreviewVO toPreview(Article article, ArticleListQuery request) {
		var vo = createPreviewFrom(article);
		if (request.isContent()) {
			vo.content = article.getContent();
		}

		var categoryPath = categoryRepository.get(article.getCategory()).getPathTo(request.getCategory());
		vo.categories = mapCategoryPath(categoryPath);

		vo.discussionCount = discussionRepository.count(new DiscussionQuery().setObjectId(article.getId()).setType(1));
		return vo;
	}

	// 排除内容属性，由外层的方法决定是否复制
	@Mapping(target = "content", ignore = true)
	abstract PreviewVO createPreviewFrom(Article article);

	abstract List<CategoryNode> mapCategoryPath(List<Category> categories);

	/**
	 * 由发表请求创建文章对象，是文章的工厂方法。
	 *
	 * @param data 发表请求
	 * @return 文章对象
	 */
	public final Article createArticle(PublishDTO data) {
		var article = new Article();
		update(article, data);
		article.setUrlTitle(processUrlTitle(data.urlTitle));
		return article;
	}

	/**
	 * 处理一下 URL 标题，使其更适合显示在 URL 里。
	 *
	 * @param value 原始 URL 标题
	 * @return 处理后的字符串
	 */
	final String processUrlTitle(String value) {
		value = urlKeywords.matcher(value).replaceAll("-");
		return StringUtils.trimTrailingCharacter(value, '-');
	}

	/**
	 * 从 DTO 请求更新文章对象相应的属性。
	 *
	 * @param article 文章
	 * @param data 更新请求
	 */
	public abstract void update(@MappingTarget Article article, PublishDTO data);
}
