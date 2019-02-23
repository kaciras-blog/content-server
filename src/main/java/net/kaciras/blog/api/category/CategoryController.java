package net.kaciras.blog.api.category;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
class CategoryController {

	private final CategoryService service;
	private final CategoryMapper mapper;

	@GetMapping("/{id}")
	public CategoryVo get(@PathVariable int id) {
		return mapper.categoryView(service.get(id));
	}

	@GetMapping("/{id}/children")
	public List<CategoryVo> getChildren(@PathVariable int id) {
		return mapper.categoryView(service.get(id).getChildren());
	}

	@PostMapping
	public ResponseEntity<Void> create(@RequestBody CategoryAttributes category, @RequestParam int parent) {
		var id = service.create(mapper.toCategory(category), parent);
		return ResponseEntity.created(URI.create("/categories/" + id)).build();
	}

	@PostMapping("/transfer")
	public void move(@RequestParam int id, @RequestParam int parent, @RequestParam boolean treeMode) {
		service.move(id, parent, treeMode);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Void> update(@PathVariable int id, @RequestBody CategoryAttributes attributes) {
		var category = service.get(id);
		mapper.update(category, attributes);
		service.update(category);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable int id, @RequestParam boolean tree) {
		service.delete(id, tree);
		return ResponseEntity.noContent().build();
	}
}
