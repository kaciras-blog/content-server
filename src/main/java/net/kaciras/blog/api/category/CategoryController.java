package net.kaciras.blog.api.category;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
class CategoryController {

	private final CategoryService categoryService;
	private final CategoryMapper mapper;

	@GetMapping("/{id}")
	public CategoryVo get(@PathVariable int id) {
		return mapper.aggregatedView(categoryService.getById(id));
	}

	@GetMapping("/{id}/children")
	public List<CategoryVo> getChildren(@PathVariable int id) {
		return mapper.categoryView(categoryService.getChildren(id));
	}

	@RequireAuthorize
	@PostMapping
	public ResponseEntity<Void> create(@RequestBody CategoryAttributes category, @RequestParam int parent) {
		int id = categoryService.add(category, parent);
		return ResponseEntity.created(URI.create("/categories/" + id)).build();
	}

	@RequireAuthorize
	@PostMapping("/transfer")
	public void move(@RequestParam int id, @RequestParam int parent, @RequestParam boolean treeMode) {
		categoryService.move(id, parent, treeMode);
	}

	@RequireAuthorize
	@PutMapping("/{id}")
	public ResponseEntity<Void> update(@PathVariable int id, @RequestBody CategoryAttributes attributes) {
		categoryService.update(id, attributes);
		return ResponseEntity.noContent().build();
	}

	@RequireAuthorize
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable int id) {
		categoryService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
