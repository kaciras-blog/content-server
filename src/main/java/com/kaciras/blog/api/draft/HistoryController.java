package com.kaciras.blog.api.draft;

import com.kaciras.blog.infra.principal.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequirePermission
@RequiredArgsConstructor
@RestController
@RequestMapping("/drafts/{id}/histories")
class HistoryController {

	private final DraftRepository repository;

	@GetMapping
	public List<History> getHistories(@PathVariable int id) {
		return repository.findById(id).getHistoryList().findAll();
	}

	@GetMapping("/{saveCount}")
	public History getHistory(@PathVariable int id, @PathVariable int saveCount) {
		return repository.findById(id)
				.getHistoryList()
				.findBySaveCount(saveCount);
	}

	@PostMapping
	public ResponseEntity<Void> saveNew(@PathVariable int id, @RequestBody DraftContent request) {
		var saveCount = repository.findById(id).getHistoryList().add(request);
		var location = "/drafts/" + id + "/histories/" + saveCount;
		return ResponseEntity.created(URI.create(location)).build();
	}

	// saveCount 没用着，目前只更新最后一次历史
	@PutMapping("/{saveCount}")
	public ResponseEntity<Void> save(@PathVariable int id, @RequestBody DraftContent request) {
		repository.findById(id).getHistoryList().update(request);
		return ResponseEntity.noContent().build();
	}
}
