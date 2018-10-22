package net.kaciras.blog.api.category;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.article.ArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
final class CategoryController {

	private final ArticleService articleService;
	private final CategoryService categoryService;
	private final CategoryMapper mapper;

	@GetMapping("/{id}")
	public CategoryVo get(@PathVariable int id) {
		return aggregate(categoryService.getById(id));
	}

	private CategoryVo aggregate(Category category) {
		var result = new AggregationVo();
		mapper.copyProps(result, category);

		result.setArticleCount(articleService.getCountByCategories(category.getId()));
		result.setBanner(categoryService.getBanner(category));
		return result;
	}

	@GetMapping("/{id}/children")
	public Flux<CategoryVo> getChildren(@PathVariable int id) {
		return Flux.fromIterable(categoryService.getChildren(id))
				.map(mapper::categoryView)
				.doOnNext(vo -> vo.setArticleCount(articleService.getCountByCategories(vo.getId())));
	}

	@PostMapping
	public ResponseEntity<Void> create(@RequestBody CategoryAttributes category, @RequestParam int parent) {
		int id = categoryService.add(category, parent);
		return ResponseEntity.created(URI.create("/categories/" + id)).build();
	}

	@PostMapping("/transfer")
	public void move(@RequestParam int id, @RequestParam int parent, @RequestParam boolean treeMode) {
		categoryService.move(id, parent, treeMode);
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
