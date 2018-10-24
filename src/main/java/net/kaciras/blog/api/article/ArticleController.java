package net.kaciras.blog.api.article;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.category.CategoryService;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
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
	public List<PreviewVo> getList(ArticleListQuery request, Pageable pageable) {
		request.setPageable(pageable);
		return articleService.getList(request);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ArticleVo> get(@PathVariable int id, WebRequest request,
										 @RequestParam(defaultValue = "false") boolean rv) {
		var etag = etagCache.get(id);
		if (request.checkNotModified(etag)) {
			return ResponseEntity.status(304).build();
		}

		var article = articleService.getArticle(id);
		if (rv) {
			article.recordView(); //增加浏览量
		}

		var vo = pojoMapper.toViewObject(article);
		vo.setNext(article.getNextLink());
		vo.setPrev(article.getPreviousLink());
		vo.setBanner(categoryService.getBanner(article.getCategory()));

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

	@RequireAuthorize
	@PostMapping
	public ResponseEntity<Void> post(@RequestBody @Valid ArticlePublishRequest request) {
		var id = articleService.publish(request);
		return ResponseEntity.created(URI.create("/articles/" + id)).build();
	}

	@RequireAuthorize
	@PutMapping("/{id}")
	public ResponseEntity<Void> update(@PathVariable int id, @RequestBody ArticlePublishRequest publish) {
		articleService.update(id, publish);
		return ResponseEntity.noContent().build();
	}

	@RequireAuthorize
	@PatchMapping("/{id}")
	public ResponseEntity<Void> updateCategories(@PathVariable int id, @RequestBody PatchMap props) {
		if (props.getCategory() != null) {
			articleService.changeCategory(id, props.getCategory());
		}
		if (props.getDeletion() != null) {
			articleService.updateDeleteion(id, props.getDeletion());
		}
		return ResponseEntity.noContent().build();
	}
}
