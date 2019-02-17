package net.kaciras.blog.api.category;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
class CategoryController {

	private final CategoryRepository repository;
	private final CategoryMapper mapper;

	@GetMapping("/{id}")
	public CategoryVo get(@PathVariable int id, @RequestParam(defaultValue = "false") boolean aggregate) {
		var category = getRequired(id);
		return aggregate ? mapper.aggregatedView(category) : mapper.categoryView(category);
	}

	@GetMapping("/{id}/children")
	public List<CategoryVo> getChildren(@PathVariable int id) {
		return mapper.categoryView(getRequired(id).getChildren());
	}

	@RequireAuthorize
	@PostMapping
	public ResponseEntity<Void> create(@RequestBody CategoryAttributes category, @RequestParam int parent) {
		try {
			var id = repository.add(mapper.toCategory(category), parent);
			return ResponseEntity.created(URI.create("/categories/" + id)).build();
		} catch (DataIntegrityViolationException ex) {
			throw new IllegalArgumentException("分类实体中存在不合法的属性值", ex);
		}
	}

	@Transactional
	@RequireAuthorize
	@PostMapping("/transfer")
	public void move(@RequestParam int id, @RequestParam int parent, @RequestParam boolean treeMode) {
		var category = getRequired(id);
		var newParent = getRequired(parent);

		if (treeMode) {
			category.moveTreeTo(newParent);
		} else {
			category.moveTo(newParent);
		}
	}

	@RequireAuthorize
	@PutMapping("/{id}")
	public ResponseEntity<Void> update(@PathVariable int id, @RequestBody CategoryAttributes attributes) {
		var category = getRequired(id);
		mapper.update(category, attributes);
		repository.update(category);
		return ResponseEntity.noContent().build();
	}

	@RequireAuthorize
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable int id, @RequestParam boolean tree) {
		if (tree) {
			repository.removeTree(id);
		} else {
			repository.remove(id);
		}
		return ResponseEntity.noContent().build();
	}

	private Category getRequired(int id) {
		return repository.get(id).orElseThrow(ResourceNotFoundException::new);
	}
}
