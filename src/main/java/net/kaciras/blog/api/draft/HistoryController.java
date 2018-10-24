package net.kaciras.blog.api.draft;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequireAuthorize
@RequiredArgsConstructor
@RestController
@RequestMapping("/drafts/{id}/histories")
public class HistoryController {

	private final DraftService draftService;

	@GetMapping
	public List<DraftHistory> getHistories(@PathVariable int id) {
		return draftService.getHistories(id);
	}

	@GetMapping("/{saveCount}")
	public DraftHistory getHistory(@PathVariable int id, @PathVariable int saveCount) {
		return draftService.getHistory(id, saveCount);
	}

	@PostMapping
	public ResponseEntity<Void> saveNew(@RequestBody DraftSaveRequest request) {
		var saveCount = draftService.saveNew(request);
		var location = "/drafts/" + request.getId() + "/histories/" + saveCount;
		return ResponseEntity.created(URI.create(location)).build();
	}

	// saveCount 没用着，目前只更新最后一次历史
	@PutMapping("/{saveCount}")
	public ResponseEntity<Void> save(@RequestBody DraftSaveRequest request) {
		draftService.save(request);
		return ResponseEntity.noContent().build();
	}
}
