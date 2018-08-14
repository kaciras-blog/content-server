package net.kaciras.blog.controller;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.pojo.DraftHistoryVo;
import net.kaciras.blog.pojo.DraftPreviewVo;
import net.kaciras.blog.pojo.DraftVo;
import net.kaciras.blog.pojo.PojoMapper;
import net.kaciras.blog.domain.SecurtyContext;
import net.kaciras.blog.domain.draft.DraftSaveDTO;
import net.kaciras.blog.domain.draft.DraftService;
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

	private final PojoMapper mapper;
	private final DraftService draftService;

	@GetMapping
	public List<DraftPreviewVo> getList() {
		return mapper.toDraftPreviewVOList(draftService.getList(SecurtyContext.getRequiredCurrentUser()));
	}

	@GetMapping("/{id}")
	public DraftVo get(@PathVariable("id") int id) {
		return mapper.draftView(draftService.get(id));
	}

	@GetMapping("/{id}/histories")
	public List<DraftHistoryVo> getHistories(@PathVariable int id) {
		return mapper.toDraftHistoryVOList(draftService.getHistories(id));
	}

	@PostMapping
	public ResponseEntity<Void> createDraft(@RequestParam(required = false) Integer article) throws URISyntaxException {
		int id = draftService.newDraft(article);
		return ResponseEntity.created(new URI("/drafts/" + id)).build();
	}

	@PutMapping("/{id}")
	public ResponseEntity<Void> put(@RequestBody DraftSaveDTO dto) {
		draftService.save(dto);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/histories")
	public ResponseEntity<Void> postHistories(@RequestBody DraftSaveDTO dto) throws URISyntaxException {
		int saveCount = draftService.saveNewHistory(dto);
		return ResponseEntity.created(new URI("/drafts/" + dto.getId() + "/histories/" + saveCount)).build();
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
