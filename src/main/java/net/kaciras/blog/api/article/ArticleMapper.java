package net.kaciras.blog.api.article;

import net.kaciras.blog.api.MapStructConfig;
import net.kaciras.blog.api.category.CategoryService;
import net.kaciras.blog.api.discuss.DiscussionQuery;
import net.kaciras.blog.api.discuss.DiscussionService;
import net.kaciras.blog.api.user.UserService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(config = MapStructConfig.class)
abstract class ArticleMapper {

	@Autowired
	private DiscussionService discussionService;

	@Autowired
	private UserService userService;

	@Autowired
	private CategoryService categoryService;


	public ArticleVo toViewObject(Article article) {
		var vo = createVoFrom(article);
		vo.setNext(article.getNextLink());
		vo.setPrev(article.getPreviousLink());
		vo.setBanner(categoryService.getBanner(article.getCategory()));
		return vo;
	}

	/**
	 * 将用户信息，评论数，分类路径和文章聚合为一个对象，节约前端请求次数。
	 *
	 * @param article 文章对象
	 * @return 聚合后的对象
	 */
	public PreviewVo toPreview(Article article, ArticleListQuery request) {
		var vo = createPreviewFrom(article);
		vo.setAuthor(userService.getUser(article.getUserId()));
		vo.setDcnt(discussionService.count(DiscussionQuery.byArticle(article.getId())));
		vo.setCpath(categoryService.getPath(article.getCategory(), request.getCategory()));
		return vo;
	}

	@Mapping(target = "vcnt", source = "viewCount")
	abstract PreviewVo createPreviewFrom(Article article);

	abstract ArticleVo createVoFrom(Article article);

	public abstract Article toArticle(ArticleContentBase contentBase);

	public abstract void update(@MappingTarget Article article, ArticleContentBase contentBase);
}
