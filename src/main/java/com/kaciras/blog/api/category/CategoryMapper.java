package com.kaciras.blog.api.category;

import com.kaciras.blog.api.MapStructConfig;
import com.kaciras.blog.api.article.ArticleListQuery;
import com.kaciras.blog.api.article.ArticleRepository;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(config = MapStructConfig.class)
abstract class CategoryMapper {

	@Autowired
	private ArticleRepository articleRepository;

	@Autowired
	private CategoryManager categoryManager;

	public AggregationVO aggregatedView(Category category) {
		if (category == null) {
			return null;
		}
		var vo = new AggregationVO();
		copyProps(vo, category);
		vo.children = categoryView(category.getChildren());
		return vo;
	}

	@Named("CategoryVo")
	public CategoryVO categoryView(Category category) {
		if (category == null) {
			return null;
		}
		var vo = new CategoryVO();
		copyProps(vo, category);
		return vo;
	}

	private void copyProps(CategoryVO vo, Category category) {
		copyPropsInternal(vo, category);

		var query = new ArticleListQuery();
		query.setCategory(category.getId());
		query.setRecursive(true);
		vo.articleCount = articleRepository.count(query);
		vo.banner = categoryManager.getBanner(category);
	}

	abstract void copyPropsInternal(@MappingTarget CategoryVO aggregation, Category category);

	@IterableMapping(qualifiedByName = "CategoryVo")
	abstract List<CategoryVO> categoryView(List<Category> list);

	abstract Category toCategory(CategoryAttributes viewObject);

	abstract void update(@MappingTarget Category category, CategoryAttributes attributes);
}
