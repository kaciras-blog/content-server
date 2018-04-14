package net.kaciras.blog.facade.controller;

import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.article.Article;
import net.kaciras.blog.domain.article.ArticleListRequest;
import net.kaciras.blog.domain.article.ArticlePublishDTO;
import net.kaciras.blog.domain.article.ArticleService;
import net.kaciras.blog.domain.category.CategoryService;
import net.kaciras.blog.domain.discuss.DiscussionQuery;
import net.kaciras.blog.domain.discuss.DiscussionService;
import net.kaciras.blog.domain.user.UserService;
import net.kaciras.blog.facade.pojo.ArticlePreviewVO;
import net.kaciras.blog.facade.pojo.ArticleVO;
import net.kaciras.blog.facade.pojo.PojoMapper;
import net.kaciras.blog.infrastructure.event.article.ArticleUpdatedEvent;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/articles")
public final class ArticleController {

	private final ArticleService articleService;
	private final UserService userService;
	private final CategoryService categoryService;
	private final DiscussionService discussionService;

	private final PojoMapper mapper;
	private final MessageClient messageClient;

	private Cache<Integer, String> etags;

	@Autowired
	public void setCacheManager(CacheManager cacheManager) {
		CacheConfigurationBuilder<Integer, String> builder = CacheConfigurationBuilder
				.newCacheConfigurationBuilder(Integer.class, String.class, ResourcePoolsBuilder.heap(100));
		etags = cacheManager.createCache("articleEtag", builder.build());
		messageClient.subscribe(ArticleUpdatedEvent.class, event -> etags.remove(event.getArticleId()));
	}

	@GetMapping
	public Observable<ArticlePreviewVO> getList(ArticleListRequest request) {
		return articleService.getList(request).map(this::assembly);
	}

	private ArticlePreviewVO assembly(Article article) {
		ArticlePreviewVO vo = mapper.toPreviewVo(article);
		vo.setDiscussionCount(discussionService.count(DiscussionQuery.byArticle(article.getId())));

		//这句没法用doOnNext
		vo.setCategoryPath(mapper.toCategoryVOList(categoryService.getPath(article.getCategories().get(0))));
		vo.setAuthor(mapper.toUserVo(userService.getUser(article.getUserId())));
		return vo;
	}

	@GetMapping("/{id}")
	public Single<ResponseEntity<ArticleVO>> get(@PathVariable int id, WebRequest request) {
		String etag = etags.get(id);

		if (request.checkNotModified(etag)) {
			return Single.just(ResponseEntity.status(304).build());
		}
		Single<ArticleVO> single = articleService.getArticle(id).map(mapper::toVO);

		/*
		 * 如果缓存中不存在，则需要创建新的缓存记录。在并发的情况下，使用
		 * etags.putIfAbsent(...)使缓存以先创建的为准，那么后创建的线程将发送一个
		 * 无效的Etag值到响应中，但对于缓存系统来说这是允许的，无效的Etag头将在
		 * 下一次访问时被重新设置。
		 */
		if (etag == null) {
			String newEtag = UUID.randomUUID().toString();
			return single
					.doOnSuccess(post -> etags.putIfAbsent(id, newEtag))
					.map(vo -> ResponseEntity.ok().eTag("W/\"" + newEtag).body(vo));
		}

		//缓存已存在，但是客户端没有记录，则发送已缓存的Etag到客户端
		return single.map(vo -> ResponseEntity.ok().eTag("W/\"" + etag).body(vo));
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

	@PutMapping("/{id}/deleteion")
	public ResponseEntity<Void> delete(@PathVariable int id, @RequestParam boolean value) {
		articleService.updateDeleteion(id, value);
		return ResponseEntity.noContent().build();
	}
}
