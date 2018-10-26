package net.kaciras.blog.api.category;

import net.kaciras.blog.api.MapStructConfig;
import net.kaciras.blog.api.article.ArticleService;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.List;

@Mapper(config = MapStructConfig.class)
abstract class CategoryMapper {

	@Autowired
	@Lazy
	private ArticleService articleService;

	@Autowired
	@Lazy
	private CategoryService categoryService;

	public AggregationVo aggregatedView(Category category) {
		var result = new AggregationVo();
		copyProps(result, category);
		result.setParent(categoryView(category.getParent()));
		result.setChildren(categoryView(category.getChildren()));
		result.setBanner(categoryService.getBanner(category));
		return result;
	}

	@Named("CategoryVo")
	public CategoryVo categoryView(Category category) {
		if(category == null) {
			return null;
		}
		var vo = new CategoryVo();
		copyProps(vo, category);
		return vo;
	}

	private void copyProps(CategoryVo vo, Category category) {
		copyPropsInternal(vo, category);
		vo.setArticleCount(articleService.getCountByCategories(category.getId()));
	}

	abstract void copyPropsInternal(@MappingTarget CategoryVo aggregation, Category category);

	@IterableMapping(qualifiedByName = "CategoryVo")
	abstract List<CategoryVo> categoryView(List<Category> list);

	abstract Category toCategory(CategoryAttributes viewObject);

	abstract void update(@MappingTarget Category category, CategoryAttributes attributes);

	abstract List<SimpleCategoryVo> simpleView(List<Category> list);
}
