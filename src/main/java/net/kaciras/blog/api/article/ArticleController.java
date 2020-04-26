package net.kaciras.blog.api.article;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kaciras.blog.api.DeletedState;
import net.kaciras.blog.api.ListQueryView;
import net.kaciras.blog.api.Utils;
import net.kaciras.blog.api.draft.DraftRepository;
import net.kaciras.blog.infra.principal.RequireAuthorize;
import net.kaciras.blog.infra.principal.SecurityContext;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/articles")
@Slf4j
@RequiredArgsConstructor
class ArticleController {

	private final ArticleRepository repository;
	private final ArticleManager articleManager;
	private final ArticleMapper mapper;

	private final DraftRepository draftRepository;

	@GetMapping
	public Object getList(ServletWebRequest request, ArticleListQuery query, Pageable pageable) {
		query.setPageable(pageable);

		/*
		 * 专门给RSS使用的缓存机制，另外如果非内部客户端请求带了content参数则发出警告。
		 *
		 * 这个API是聚合请求，除了文章外还返回了分类信息，因此不能仅以文章的更新时间来判断
		 * 是否缓存。但RSS不使用分类信息，目前也就仅对其做一个缓存。
		 *
		 * 虽然RSS请求有个分类参数，但全部文章最后更新时间一定不小于某个分类里的，所以也没错，
		 * 只是会降低缓存命中率。
		 * 但我懒得再改了，性能目前也不是什么大问题。
		 */
		if (query.isContent()) {
			var nativeRequest = request.getNativeRequest(HttpServletRequest.class);

			@SuppressWarnings("ConstantConditions")
			var remote = Utils.addressFromRequest(nativeRequest);

			if (Utils.isLocalNetwork(remote)) {
				var lastModified = repository.lastUpdate().toEpochMilli();
				if (request.checkNotModified(lastModified)) {
					return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
				}
			} else {
				query.setContent(false);
				logger.warn("来自非内部网络的请求使用了RSS专用的API，{}", remote);
			}
		}

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
		} else {
			var draft = draftRepository.findById(request.getDraftId());
			draft.setArticleId(article.getId());
			draftRepository.update(draft);
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
