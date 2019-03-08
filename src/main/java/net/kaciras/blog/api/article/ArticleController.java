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

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/articles")
class ArticleController {

	private final ArticleRepository repository;
	private final ArticleManager articleManager;
	private final ArticleMapper mapper;

	private final MessageClient messageClient;

	@GetMapping
	public List<PreviewVo> getList(ArticleListQuery request, Pageable pageable) {
		request.setPageable(pageable);
		if (request.getDeletion() != DeletedState.FALSE) {
			SecurityContext.require("SHOW_DELETED");
		}
		return mapper.toPreview(repository.findAll(request), request);
	}

	@GetMapping("/{id}")
	public ArticleVo get(@PathVariable int id, @RequestParam(defaultValue = "false") boolean rv) {
		var article = articleManager.getLiveArticle(id);
		if (rv) {
			article.recordView(); //增加浏览量
		}
		return mapper.toViewObject(article);
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
