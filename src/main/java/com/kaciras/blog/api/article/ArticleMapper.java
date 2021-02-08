package com.kaciras.blog.api.article;

import com.kaciras.blog.api.MapStructConfig;
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

	public ArticleVO toViewObject(Article article) {
		var vo = createVoFrom(article);
		article.getPrev().map(this::toLink).ifPresent(x -> vo.prev = x);
		article.getNext().map(this::toLink).ifPresent(x -> vo.next = x);
		vo.banner = categoryManager.getBanner(article.getCategory());
		return vo;
	}

	abstract ArticleLink toLink(Article article);

	public List<PreviewVO> toPreview(@NonNull List<Article> articles, ArticleListQuery request) {
		return articles.stream().map(article -> toPreview(article, request)).collect(Collectors.toList());
	}

	/**
	 * 将用户信息，评论数，分类路径和文章聚合为一个对象，节约前端请求次数。
	 *
	 * @param article 文章对象
	 * @return 聚合后的对象
	 */
	PreviewVO toPreview(Article article, ArticleListQuery request) {
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

	@Mapping(target = "prev", ignore = true)
	@Mapping(target = "next", ignore = true)
	abstract ArticleVO createVoFrom(Article article);

	abstract List<CategoryNode> mapCategoryPath(List<Category> categories);

	/**
	 * 由发表请求创建文章对象，是文章的工厂方法。
	 *
	 * @param request 发表请求
	 * @return 文章对象
	 */
	public Article createArticle(PublishDTO request) {
		var article = new Article();
		update(article, request);

		article.setCategory(request.category);
		article.setUrlTitle(StringUtils.trimTrailingCharacter(urlKeywords
				.matcher(request.urlTitle).replaceAll("-"), '-'));
		return article;
	}

	public abstract void update(@MappingTarget Article article, PublishDTO dto);
}
