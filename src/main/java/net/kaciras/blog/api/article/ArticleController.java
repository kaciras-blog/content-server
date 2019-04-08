package net.kaciras.blog.api.article;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.DeletedState;
import net.kaciras.blog.api.draft.DraftRepository;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
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

	private final DraftRepository draftRepository;

	@Value("${draft.delete-after-publish}")
	private boolean deleteAfterSubmit;

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
		var article = mapper.createArticle(request);
		repository.add(article);

		if (deleteAfterSubmit) {
			draftRepository.remove(request.getDraftId());
		}
		return ResponseEntity.created(URI.create("/articles/" + article.getId())).build();
	}

	// 不更改 urlTitle，category，这些属性使用PATCH修改
	@RequireAuthorize
	@PutMapping("/{id}")
	public ResponseEntity<Void> update(@PathVariable int id, @RequestBody PublishRequest request) {
		var article = repository.get(id);

		mapper.update(article, request);
		repository.update(article);

		if (deleteAfterSubmit) {
			draftRepository.remove(request.getDraftId());
		}
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
		if (patchMap.getUrlTitle() != null) {
			article.updateUrlTitle(patchMap.getUrlTitle());
		}
		return ResponseEntity.noContent().build();
	}
}
