package com.kaciras.blog.api.article;

import com.kaciras.blog.api.DeletedState;
import com.kaciras.blog.api.ListQueryView;
import com.kaciras.blog.api.draft.DraftContent;
import com.kaciras.blog.api.draft.DraftRepository;
import com.kaciras.blog.infra.RequestUtils;
import com.kaciras.blog.infra.exception.ResourceDeletedException;
import com.kaciras.blog.infra.principal.RequirePermission;
import com.kaciras.blog.infra.principal.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
			var remote = RequestUtils.addressFromRequest(nativeRequest);

			if (RequestUtils.isLocalNetwork(remote)) {
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
	public ArticleVO get(@PathVariable int id) {
		var article = repository.findById(id);
		if (article.isDeleted()) {
			throw new ResourceDeletedException();
		}
		article.increaseViewCount();
		return mapper.toViewObject(article);
	}

	@Transactional
	@RequirePermission
	@PostMapping
	public ResponseEntity<ArticleVO> post(@RequestBody @Valid PublishDTO request) {
		var article = mapper.createArticle(request);
		repository.add(article);
		updateDraft(article, request);

		return ResponseEntity
				.created(URI.create("/articles/" + article.getId()))
				.body(mapper.toViewObject(article));
	}

	// 不更改 urlTitle，category，这些属性使用PATCH修改
	@Transactional
	@RequirePermission
	@PutMapping("/{id}")
	public ArticleVO update(@PathVariable int id, @RequestBody PublishDTO request) {
		var article = repository.findById(id);

		mapper.update(article, request);
		repository.update(article);
		updateDraft(article, request);

		return mapper.toViewObject(article);
	}

	// 这个涉及到草稿表，调用方需要加事物
	private void updateDraft(Article article, PublishDTO request) {
		if (request.destroy) {
			draftRepository.remove(request.draftId);
		} else {
			var draft = draftRepository.findById(request.draftId);
			draft.setArticleId(article.getId());
			draftRepository.update(draft);

			var history = new DraftContent();
			history.setTitle(request.title);
			history.setKeywords(String.join(" ", request.keywords));
			history.setCover(request.cover);
			history.setSummary(request.summary);
			history.setContent(request.content);
			draft.getHistoryList().add(history);
		}
	}

	@RequirePermission
	@PatchMapping("/{id}")
	public ArticleVO patch(@PathVariable int id, @RequestBody PatchDTO patch) {
		var article = repository.findById(id);
		Optional.ofNullable(patch.category).ifPresent(article::updateCategory);
		Optional.ofNullable(patch.deletion).ifPresent(article::updateDeleted);
		Optional.ofNullable(patch.urlTitle).ifPresent(article::updateUrlTitle);
		return mapper.toViewObject(article);
	}
}
