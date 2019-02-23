package net.kaciras.blog.api.draft;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.article.ArticleRepository;
import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * 草稿相关的API
 * <p>
 * URL格式：/drafts/{id}/histories/{saveCount}
 * id：草稿id
 * saveCount: 草稿的保存序号
 */
@RequireAuthorize
@RequiredArgsConstructor
@RestController
@RequestMapping("/drafts")
class DraftController {

	private final DraftRepository repository;
	private final DraftMapper mapper;
	private final ArticleRepository articleRepository;

	@GetMapping
	public List<DraftVo> getList() {
		return mapper.toDraftVo(repository.findByUser(SecurityContext.getUserId()));
	}

	@GetMapping("/{id}")
	public DraftVo get(@PathVariable int id) {
		return repository.findById(id)
				.map(mapper::toDraftVo)
				.orElseThrow(ResourceNotFoundException::new);
	}

	/**
	 * 创建一个新的草稿，其内容可能是默认内容或从指定的文章生成。
	 *
	 * @param article 文章ID，如果不为null则从文章生成草稿
	 * @return HTTP回复
	 */
	@Transactional
	@PostMapping
	public ResponseEntity<Void> createDraft(@RequestParam(required = false) Integer article) {
		var content = Optional.ofNullable(article)
				.map(id -> articleRepository.get(article)
						.map(mapper::fromArticle)
						.orElseThrow(RequestArgumentException::new))
				.orElseGet(DraftContent::initial);

		var draft = new Draft();
		draft.setUserId(SecurityContext.getUserId());
		draft.setArticleId(article);
		repository.add(draft);

		draft.getHistoryList().add(content);
		return ResponseEntity.created(URI.create("/drafts/" + draft.getId())).build();
	}

	@DeleteMapping
	public ResponseEntity<Void> clearForUser(@RequestParam int userId) {
		repository.clear(userId);
		return ResponseEntity.status(HttpStatus.RESET_CONTENT).build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable int id) {
		repository.remove(id);
		return ResponseEntity.noContent().build();
	}
}
