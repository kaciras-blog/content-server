package com.kaciras.blog.api.category;

import com.kaciras.blog.infra.principal.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

/**
 * <h2>暂时无法实现的功能</h2>
 * 不支持删除，因为删除后分类的关联对象（文章等）迁移很麻烦。
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
class CategoryController {

	private final CategoryRepository repository;
	private final CategoryMapper mapper;

	@GetMapping("/{id}")
	public CategoryVO get(@PathVariable int id, @RequestParam(defaultValue = "false") boolean aggregate) {
		var category = repository.get(id);
		return aggregate ? mapper.aggregatedView(category) : mapper.categoryView(category);
	}

	@GetMapping("/{id}/children")
	public List<CategoryVO> getChildren(@PathVariable int id) {
		return mapper.categoryView(repository.get(id).getChildren());
	}

	@RequirePermission
	@PostMapping
	public ResponseEntity<CategoryVO> create(@RequestParam int parent, @RequestBody @Valid CreateDTO data) {
		var category = new Category();
		mapper.update(category, data);
		repository.add(category, parent);

		return ResponseEntity
				.created(URI.create("/categories/" + category.getId()))
				.body(mapper.categoryView(category));
	}

	@RequirePermission
	@Transactional
	@PostMapping("/transfer")
	public ResponseEntity<Void> move(@RequestBody MoveDTO moveDTO) {
		var category = repository.get(moveDTO.id);
		var newParent = repository.get(moveDTO.parent);

		if (moveDTO.treeMode) {
			category.moveTreeTo(newParent);
		} else {
			category.moveTo(newParent);
		}

		return ResponseEntity.noContent().build();
	}

	@RequirePermission
	@PutMapping("/{id}")
	public CategoryVO update(@PathVariable int id, @RequestBody @Valid CreateDTO data) {
		var category = repository.get(id);
		mapper.update(category, data);
		repository.update(category);
		return mapper.categoryView(category);
	}
}
