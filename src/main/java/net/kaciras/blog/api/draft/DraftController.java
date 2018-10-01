package net.kaciras.blog.api.draft;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * 草稿相关的API
 * <p>
 * URL格式：/drafts/{id}/histories/{saveCount}
 * id：草稿id
 * saveCount: 草稿的保存序号
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/drafts")
final class DraftController {

	private final DraftMapper mapper;
	private final DraftService draftService;

	@GetMapping
	public List<DraftPreviewVo> getList() {
		return mapper.toPreviewVo(draftService.getList(SecurityContext.getUserId()));
	}

	@GetMapping("/{id}")
	public DraftVo get(@PathVariable("id") int id) {
		return mapper.toVo(draftService.get(id));
	}

	@GetMapping("/{id}/histories")
	public List<DraftHistoryVo> getHistories(@PathVariable int id) {
		return mapper.toDraftHistoryVOList(draftService.getHistories(id));
	}

	@PostMapping
	public ResponseEntity<Void> createDraft(@RequestParam(required = false) Integer article) throws URISyntaxException {
		var id = draftService.newDraft(article);
		return ResponseEntity.created(new URI("/drafts/" + id)).build();
	}

	@PostMapping("/{id}/histories")
	public ResponseEntity<Void> save(@RequestBody DraftSaveRequest request) throws URISyntaxException {
		var saveCount = draftService.save(request);
		return ResponseEntity.created(new URI("/drafts/" + request.getId() + "/histories/" + saveCount)).build();
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
