package net.kaciras.blog.category;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
interface CategoryMapper {

	@Named("Category")
	CategoryVo categoryView(Category category);

	@IterableMapping(qualifiedByName = "Category")
	List<CategoryVo> categoryView(List<Category> histories);

	Category toCategory(CategoryVo vo);
}
