package net.kaciras.blog.api.category;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.article.ArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
final class CategoryController {

	private final ArticleService articleService;
	private final CategoryService categoryService;
	private final CategoryMapper mapper;

	@PostMapping("/transfer")
	public void move(@RequestParam int id, @RequestParam int parent, @RequestParam boolean treeMode) {
		categoryService.move(id, parent, treeMode);
	}

	@GetMapping("/{id}")
	public CategoryVo get(@PathVariable int id) {
		var vo = mapper.categoryView(categoryService.get(id));
		vo.setArticleCount(articleService.getCountByCategories(id));
		return vo;
	}

	@GetMapping("/{id}/children")
	public Flux<CategoryVo> getChildren(@PathVariable int id) {
		return Flux.fromIterable(categoryService.getChildren(id))
				.map(mapper::categoryView)
				.doOnNext(vo -> vo.setArticleCount(articleService.getCountByCategories(vo.getId())));
	}

	@GetMapping("{id}/path")
	public List<CategoryVo> getPath(@PathVariable int id) {
		return categoryService.getPath(id);
	}

	@PostMapping
	public ResponseEntity<Void> create(@RequestBody CategoryVo category) {
		int id = categoryService.add(category, category.getParent());
		return ResponseEntity.created(URI.create("/categories/" + id)).build();
	}

	@PutMapping("/{id}")
	public ResponseEntity<Void> update(@PathVariable int id, @RequestBody CategoryAttributes attributes) {
		categoryService.update(id, attributes);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable int id) {
		categoryService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
