package net.kaciras.blog.controller;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.pojo.CategoryVo;
import net.kaciras.blog.pojo.PojoMapper;
import net.kaciras.blog.domain.article.ArticleService;
import net.kaciras.blog.domain.category.Category;
import net.kaciras.blog.domain.category.CategoryService;
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
	private final PojoMapper mapper;

	@PostMapping("/transfer")
	public void move(@RequestParam int id, @RequestParam int parent, @RequestParam boolean treeMode) {
		categoryService.moveTree(id, parent, treeMode);
	}

//	获取所有的分类？
//	@GetMapping
//	public List<Category> getTopLayer() {
//		return categoryService.getTopCategories();
//	}

	@GetMapping("/{id}")
	public CategoryVo get(@PathVariable int id) {
		var vo = mapper.categoryView(categoryService.get(id));
		vo.setArticleCount(articleService.getCountByCategories(id));
		return vo;
	}

	@GetMapping("/{id}/subCategories")
	public Flux<CategoryVo> getChildren(@PathVariable int id) {
		return Flux.fromIterable(categoryService.getSubCategories(id))
				.map(mapper::categoryView)
				.doOnNext(vo -> vo.setArticleCount(articleService.getCountByCategories(vo.getId())));
	}

	@GetMapping("{id}/path")
	public List<CategoryVo> getPath(@PathVariable int id) {
		return mapper.categoryView(categoryService.getPath(id));
	}

	@PostMapping
	public ResponseEntity<Void> create(@RequestBody CategoryVo category) {
		int id = categoryService.add(mapper.toCategory(category), category.getParent());
		return ResponseEntity.created(URI.create("/categories/" + id)).build();
	}

	@PutMapping("/{id}")
	public ResponseEntity<Void> update(@PathVariable int id, @RequestBody Category category) {
		category.setId(id);
		categoryService.update(category);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable int id) {
		categoryService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
