package com.kaciras.blog.api.category;

import com.kaciras.blog.api.MapStructConfig;
import com.kaciras.blog.api.article.ArticleListQuery;
import com.kaciras.blog.api.article.ArticleRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(config = MapStructConfig.class)
abstract class CategoryMapper {

	@Autowired
	private ArticleRepository articleRepository;

	@Autowired
	private CategoryManager categoryManager;

	public final CategoryVO aggregatedView(Category category) {
		if (category == null) {
			return null;
		}
		var vo = new CategoryVO();
		copyProps(vo, category);
		vo.children = categoryView(category.getChildren());
		return vo;
	}

	@Named("CategoryVo")
	public final CategoryVO categoryView(Category category) {
		if (category == null) {
			return null;
		}
		var vo = new CategoryVO();
		copyProps(vo, category);
		return vo;
	}

	private void copyProps(CategoryVO vo, Category category) {
		copyPropsInternal(vo, category);
		vo.banner = categoryManager.getBanner(category);

		var query = new ArticleListQuery();
		query.setCategory(category.getId());
		query.setRecursive(true);
		vo.articleCount = articleRepository.count(query);
	}

	@Mapping(target = "children", ignore = true)
	abstract void copyPropsInternal(@MappingTarget CategoryVO aggregation, Category category);

	@IterableMapping(qualifiedByName = "CategoryVo")
	public abstract List<CategoryVO> categoryView(List<Category> list);

	public abstract void update(@MappingTarget Category category, CreateDTO data);
}
