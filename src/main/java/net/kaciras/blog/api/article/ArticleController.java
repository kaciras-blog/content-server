package net.kaciras.blog.api.article;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.DeletedState;
import net.kaciras.blog.api.ListQueryView;
import net.kaciras.blog.api.article.model.ArticleListQuery;
import net.kaciras.blog.api.article.model.ArticleManager;
import net.kaciras.blog.api.article.model.ArticleRepository;
import net.kaciras.blog.api.draft.DraftRepository;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/articles")
class ArticleController {

	private final ArticleRepository repository;
	private final ArticleManager articleManager;
	private final ArticleMapper mapper;

	private final DraftRepository draftRepository;

	@GetMapping
	public ListQueryView<PreviewVo> getList(ArticleListQuery query, Pageable pageable) {
		query.setPageable(pageable);
		if (query.getDeletion() != DeletedState.ALIVE) {
			SecurityContext.require("SHOW_DELETED");
		}
		var items = mapper.toPreview(repository.findAll(query), query);
		var total = repository.count(query);

		return new ListQueryView<>(total, items);
	}

	@GetMapping("/{id}")
	public ArticleVo get(@PathVariable int id) {
		var article = articleManager.getLiveArticle(id);
		article.increaseViewCount();
		return mapper.toViewObject(article);
	}

	@RequireAuthorize
	@PostMapping
	public ResponseEntity<ArticleVo> post(@RequestBody @Valid PublishInput request) {
		var article = mapper.createArticle(request);
		repository.add(article);

		if (request.isDestroy()) {
			draftRepository.remove(request.getDraftId());
		}
		return ResponseEntity
				.created(URI.create("/articles/" + article.getId()))
				.body(mapper.toViewObject(article));
	}

	// 不更改 urlTitle，category，这些属性使用PATCH修改
	@RequireAuthorize
	@PutMapping("/{id}")
	public ArticleVo update(@PathVariable int id, @RequestBody PublishInput request) {
		var article = repository.get(id);

		mapper.update(article, request);
		repository.update(article);

		if (request.isDestroy()) {
			draftRepository.remove(request.getDraftId());
		}
		return mapper.toViewObject(article);
	}

	@RequireAuthorize
	@PatchMapping("/{id}")
	public ArticleVo patch(@PathVariable int id, @RequestBody PatchInput patchInput) {
		var article = repository.get(id);
		Optional.ofNullable(patchInput.getCategory()).ifPresent(article::updateCategory);
		Optional.ofNullable(patchInput.getDeletion()).ifPresent(article::updateDeleted);
		Optional.ofNullable(patchInput.getUrlTitle()).ifPresent(article::updateUrlTitle);
		return mapper.toViewObject(article);
	}
}
