package net.kaciras.blog.api.article;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.category.CategoryService;
import net.kaciras.blog.api.discuss.DiscussionQuery;
import net.kaciras.blog.api.discuss.DiscussionService;
import net.kaciras.blog.infrastructure.event.article.ArticleUpdatedEvent;
import net.kaciras.blog.infrastructure.message.MessageClient;
import net.kaciras.blog.api.user.UserService;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/articles")
final class ArticleController {

	private final ArticleService articleService;
	private final UserService userService;
	private final CategoryService categoryService;
	private final DiscussionService discussionService;

	private final ArticleMapper pojoMapper;
	private final MessageClient messageClient;

	private Cache<Integer, String> etagCache;

	@Autowired
	public void setCacheManager(CacheManager cacheManager) {
		CacheConfigurationBuilder<Integer, String> builder = CacheConfigurationBuilder
				.newCacheConfigurationBuilder(Integer.class, String.class, ResourcePoolsBuilder.heap(4096));
		etagCache = cacheManager.createCache("articleEtag", builder.build());
		messageClient.subscribe(ArticleUpdatedEvent.class, event -> etagCache.remove(event.getArticleId()));
	}


	@GetMapping
	public List<PreviewVo> getList(ArticleListRequest request, Pageable pageable) {
		request.setPageable(pageable);
		return articleService.getList(request).stream().map(this::aggregate).collect(Collectors.toList());
	}

	/**
	 * 将用户信息，评论数，分类路径和文章聚合为一个对象，节约前端请求次数。
	 *
	 * @param article 文章对象
	 * @return 聚合后的对象
	 */
	private PreviewVo aggregate(Article article) {
		var result = pojoMapper.toPreview(article);
		result.setAuthor(userService.getUser(article.getUserId()));
		result.setDcnt(discussionService.count(DiscussionQuery.byArticle(article.getId())));
		result.setCpath(categoryService.getPath(article.getCategories().get(0)));
		return result;
	}

	@GetMapping("/{id}")
	public ResponseEntity<ArticleVo> get(@PathVariable int id, WebRequest request) {
		var etag = etagCache.get(id);

		if (request.checkNotModified(etag)) {
			return ResponseEntity.status(304).build();
		}
		var article = pojoMapper.toViewObject(articleService.getArticle(id));

		/*
		 * 如果缓存中不存在，则需要创建新的缓存记录。在并发的情况下，使用
		 * etagCache.putIfAbsent(...)使缓存以先创建的为准，那么后创建的线程将发送一个
		 * 无效的Etag值到响应中，但对于缓存系统来说这是允许的，无效的Etag头将在
		 * 下一次访问时被重新设置。
		 */
		if (etag == null) {
			etag = UUID.randomUUID().toString();
			etagCache.putIfAbsent(id, etag);
			return ResponseEntity.ok().eTag("W/\"" + etag).body(article);
		}

		//缓存已存在，但是客户端没有记录，则添加已缓存的Etag到客户端
		return ResponseEntity.ok().eTag("W/\"" + etag).body(article);
	}

	@PostMapping
	public ResponseEntity<Void> post(@RequestBody ArticlePublishDTO dto) throws URISyntaxException {
		int id = articleService.publish(dto);
		return ResponseEntity.created(new URI("/articles/" + id)).build();
	}

	/*
	 * 用PUT，那么当分类字段不存在时应当清除资源的分类信息，
	 * 如果用PATCH，底层就必须能够单独更新资源的字段，以忽略请求中不存在的字段。
	 * 目前看来那种都不好使...
	 */
	@PutMapping("/{id}")
	public ResponseEntity<Void> update(@PathVariable int id, @RequestBody ArticlePublishDTO publish) {
		articleService.update(id, publish);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/categories")
	public ResponseEntity<Void> updateCategories(@PathVariable int id, @RequestBody List<Integer> cates) {
		articleService.changeCategory(id, cates);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/deletion")
	public ResponseEntity<Void> delete(@PathVariable int id, @RequestParam boolean value) {
		articleService.updateDeleteion(id, value);
		return ResponseEntity.noContent().build();
	}
}