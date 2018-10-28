package net.kaciras.blog.api.article;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.DeletedState;
import net.kaciras.blog.infrastructure.event.article.ArticleCreatedEvent;
import net.kaciras.blog.infrastructure.event.article.ArticleUpdatedEvent;
import net.kaciras.blog.infrastructure.message.MessageClient;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
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

	private final ArticleRepository repository;
	private final ArticleManager articleManager;
	private final ArticleMapper mapper;

	private final MessageClient messageClient;

	private Map<Integer, String> etagCache = new ConcurrentHashMap<>();

	// TODO: messaging system
	@SuppressWarnings("FieldCanBeLocal")
	private boolean disableCache = true;

	@GetMapping
	public List<PreviewVo> getList(ArticleListQuery request, Pageable pageable) {
		request.setPageable(pageable);
		if (request.getDeletion() != DeletedState.FALSE) {
			SecurityContext.require("SHOW_DELETED");
		}
		return mapper.toPreview(repository.findAll(request), request);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ArticleVo> get(@PathVariable int id, WebRequest request,
										 @RequestParam(defaultValue = "false") boolean rv) {
		var etag = etagCache.get(id);
		if (request.checkNotModified(etag)) {
			return ResponseEntity.status(304).build();
		}

		var article = articleManager.getLiveArticle(id);
		var vo = mapper.toViewObject(article);

		if (rv) {
			article.recordView(); //增加浏览量
		}
		if (disableCache) {
			return ResponseEntity.ok().body(vo);
		}

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
	public ResponseEntity<Void> post(@RequestBody @Valid PublishRequest request) {
		var article = mapper.createArticle(request, SecurityContext.getUserId());
		repository.add(article);

		messageClient.send(new ArticleCreatedEvent(article.getId(), request.getDraftId(), request.getCategory()));
		return ResponseEntity.created(URI.create("/articles/" + article.getId())).build();
	}

	// 不更改 urlTitle，category，这些属性使用PATCH修改
	@RequireAuthorize
	@PutMapping("/{id}")
	public ResponseEntity<Void> update(@PathVariable int id, @RequestBody PublishRequest update) {
		var article = repository.get(id);

		mapper.update(article, update);
		repository.update(article);

		messageClient.send(new ArticleUpdatedEvent(id, update.getDraftId(), update.getCategory()));
		return ResponseEntity.noContent().build();
	}

	@RequireAuthorize
	@PatchMapping("/{id}")
	public ResponseEntity<Void> patch(@PathVariable int id, @RequestBody PatchMap patchMap) {
		var article = repository.get(id);

		if (patchMap.getCategory() != null) {
			article.updateCategory(patchMap.getCategory());
		}
		if (patchMap.getDeletion() != null) {
			article.updateDeleted(patchMap.getDeletion());
		}
		if(patchMap.getUrlTitle() != null) {
			article.updateUrlTitle(patchMap.getUrlTitle());
		}
		return ResponseEntity.noContent().build();
	}
}
