package net.kaciras.blog.api.article;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
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

	private final ArticleService service;
	private final ArticleMapper mapper;

	private Map<Integer, String> etagCache = new ConcurrentHashMap<>();

	// TODO: messaging system
	@SuppressWarnings("FieldCanBeLocal")
	private boolean disableCache = true;

	@GetMapping
	public List<ArticleVo> getList(ArticleListQuery request, Pageable pageable) {
		request.setPageable(pageable);
		return mapper.toViewObject(service.getList(request));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ArticleVo> get(@PathVariable int id, WebRequest request,
										 @RequestParam(defaultValue = "false") boolean rv) {
		var etag = etagCache.get(id);
		if (request.checkNotModified(etag)) {
			return ResponseEntity.status(304).build();
		}

		var article = service.getArticle(id, rv);
		var vo = mapper.toViewObject(article);

		if (disableCache) {
			return ResponseEntity.ok().body(vo);
		}

		// 如果缓存中不存在，则需要创建新的缓存记录。
		if (etag == null) {
			etag = etagCache.putIfAbsent(id, UUID.randomUUID().toString());
			return ResponseEntity.ok().eTag("W/\"" + etag).body(vo);
		}

		// 缓存已存在，但是客户端没有记录，则添加已缓存的Etag到客户端
		return ResponseEntity.ok().eTag("W/\"" + etag).body(vo);
	}

	@PostMapping
	public ResponseEntity<Void> post(@RequestBody @Valid PublishRequest request) {
		var article = mapper.createArticle(request, SecurityContext.getUserId());
		service.addNew(article, request.getDraftId());
		return ResponseEntity.created(URI.create("/articles/" + article.getId())).build();
	}

	/* PATCH 一个端点处理所有？
	 * {
	 * 		attributes: {
	 * 			...ContentBase,
	 * 			urlTitle: ...
	 * 		}
	 * 		category: ...
	 * 		deletion: ...
	 * }
	 */
	@PatchMapping("/{id}")
	public ResponseEntity<Void> patch(@PathVariable int id, @RequestBody PatchMap patchMap) {
		service.update(id, patchMap);
		return ResponseEntity.noContent().build();
	}
}
