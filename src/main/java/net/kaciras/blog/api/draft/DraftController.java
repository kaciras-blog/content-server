package net.kaciras.blog.api.draft;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

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

	private final DraftMapper mapper;
	private final DraftService draftService;

	@GetMapping
	public List<DraftVo> getList() {
		return draftService.getList(SecurityContext.getUserId());
	}

	@GetMapping("/{id}")
	public DraftVo get(@PathVariable("id") int id) {
		return mapper.toVo(draftService.get(id));
	}

	@PostMapping
	public ResponseEntity<Void> createDraft(@RequestParam(required = false) Integer article) {
		var id = draftService.newDraft(article);
		return ResponseEntity.created(URI.create("/drafts/" + id)).build();
	}

	@DeleteMapping
	public ResponseEntity<Void> deleteAll(@RequestParam int userId) {
		draftService.deleteByUser(userId);
		return ResponseEntity.status(HttpStatus.RESET_CONTENT).build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable int id) {
		draftService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
