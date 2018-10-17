package net.kaciras.blog.api.article;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.category.CategoryService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@RestController
@RequestMapping("/articles")
class ArticleController {

	private final ArticleService articleService;
	private final CategoryService categoryService;

	private final ArticleMapper pojoMapper;

	private Map<Integer, String> etagCache = new ConcurrentHashMap<>();

	@GetMapping
	public List<PreviewVo> getList(ArticleListRequest request, Pageable pageable) {
		request.setPageable(pageable);
		return articleService.getList(request);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ArticleVo> get(@PathVariable int id, WebRequest request) {
		var etag = etagCache.get(id);
		if (request.checkNotModified(etag)) {
			return ResponseEntity.status(304).build();
		}

		var article = articleService.getArticle(id);
		var vo = pojoMapper.toViewObject(article);

		vo.setNext(article.getNextLink());
		vo.setPrev(article.getPreviousLink());
		vo.setBanner(categoryService.getBestBackground(article.getCategory()));

		/*
		 * 如果缓存中不存在，则需要创建新的缓存记录。在并发的情况下，使用
		 * etagCache.putIfAbsent(...)使缓存以先创建的为准，那么后创建的线程将发送一个
		 * 无效的Etag值到响应中，但对于缓存系统来说这是允许的，无效的Etag头将在
		 * 下一次访问时被重新设置。
		 */
		if (etag == null) {
			etag = UUID.randomUUID().toString();
			etagCache.putIfAbsent(id, etag);
			return ResponseEntity.ok().eTag("W/\"" + etag).body(vo);
		}

		// 缓存已存在，但是客户端没有记录，则添加已缓存的Etag到客户端
		return ResponseEntity.ok().eTag("W/\"" + etag).body(vo);
	}

	@PostMapping
	public ResponseEntity<Void> post(@RequestBody @Valid ArticlePublishRequest request) {
		var id = articleService.publish(request);
		return ResponseEntity.created(URI.create("/articles/" + id)).build();
	}

	/*
	 * 用PUT，那么当分类字段不存在时应当清除资源的分类信息，
	 * 如果用PATCH，底层就必须能够单独更新资源的字段，以忽略请求中不存在的字段。
	 * 目前看来那种都不好使...
	 */
	@PutMapping("/{id}")
	public ResponseEntity<Void> update(@PathVariable int id, @RequestBody ArticlePublishRequest publish) {
		articleService.update(id, publish);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/categories")
	public ResponseEntity<Void> updateCategories(@PathVariable int id, @RequestBody int category) {
		articleService.changeCategory(id, category);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/deletion")
	public ResponseEntity<Void> delete(@PathVariable int id, @RequestParam boolean value) {
		articleService.updateDeleteion(id, value);
		return ResponseEntity.noContent().build();
	}
}
