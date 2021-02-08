package com.kaciras.blog.api.draft;

import com.kaciras.blog.api.ListQueryView;
import com.kaciras.blog.api.article.ArticleRepository;
import com.kaciras.blog.infra.principal.RequirePermission;
import com.kaciras.blog.infra.principal.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * 草稿相关的API
 * <p>
 * URL格式：/drafts/{id}/histories/{saveCount}
 * id：草稿id
 * saveCount: 草稿的保存序号
 */
@RequirePermission
@RequiredArgsConstructor
@RestController
@RequestMapping("/drafts")
class DraftController {

	private final DraftRepository repository;
	private final DraftMapper mapper;
	private final ArticleRepository articleRepository;

	@GetMapping
	public ListQueryView<DraftVO> getList() {
		var items = repository.findByUser(SecurityContext.getUserId());
		return new ListQueryView<>(items.size(), mapper.toDraftVo(items));
	}

	@GetMapping("/{id}")
	public DraftVO get(@PathVariable int id) {
		return mapper.toDraftVo(repository.findById(id));
	}

	/**
	 * 创建一个新的草稿，其内容可能是默认内容或从指定的文章生成。
	 *
	 * @param article 文章ID，如果不为null则从文章生成草稿
	 */
	@Transactional
	@PostMapping
	public ResponseEntity<DraftVO> createDraft(@RequestParam(required = false) Integer article) {
		var content = article != null
				? mapper.fromArticle(articleRepository.findById(article))
				: DraftContent.initial();

		var draft = new Draft();
		draft.setUserId(SecurityContext.getUserId());
		draft.setArticleId(article);

		repository.add(draft);
		draft.getHistoryList().add(content);

		return ResponseEntity
				.created(URI.create("/drafts/" + draft.getId()))
				.body(mapper.toDraftVo(draft));
	}

	@DeleteMapping
	public ResponseEntity<Void> clearForUser(@RequestParam int userId) {
		repository.clear(userId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable int id) {
		repository.remove(id);
		return ResponseEntity.noContent().build();
	}
}
