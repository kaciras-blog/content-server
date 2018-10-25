package net.kaciras.blog.api.category;

import net.kaciras.blog.api.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(config = MapStructConfig.class)
interface CategoryMapper {

	void copyProps(@MappingTarget AggregationVo aggregation, Category category);

	CategoryVo categoryView(Category category);

	List<CategoryVo> categoryView(List<Category> list);

	Category toCategory(CategoryAttributes viewObject);

	void update(@MappingTarget Category category, CategoryAttributes attributes);

	List<SimpleCategoryVo> simpleView(List<Category> list);
}
