package net.kaciras.blog.api.draft;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequireAuthorize
@RequiredArgsConstructor
@RestController
@RequestMapping("/drafts/{id}/histories")
class HistoryController {

	private final DraftRepository repository;

	@GetMapping
	public List<DraftHistory> getHistories(@PathVariable int id) {
		return getRequired(id).getHistoryList().findAll();
	}

	@GetMapping("/{saveCount}")
	public DraftHistory getHistory(@PathVariable int id, @PathVariable int saveCount) {
		return getRequired(id).getHistoryList().findBySaveCount(saveCount);
	}

	@PostMapping
	public ResponseEntity<Void> saveNew(@RequestBody SaveRequest request) {
		var saveCount = getRequired(request.getId()).getHistoryList().add(request);
		var location = "/drafts/" + request.getId() + "/histories/" + saveCount;
		return ResponseEntity.created(URI.create(location)).build();
	}

	// saveCount 没用着，目前只更新最后一次历史
	@PutMapping("/{saveCount}")
	public ResponseEntity<Void> save(@RequestBody SaveRequest request) {
		getRequired(request.getId()).getHistoryList().update(request);
		return ResponseEntity.noContent().build();
	}

	private Draft getRequired(int id) {
		return repository.findById(id).orElseThrow(ResourceNotFoundException::new);
	}
}
