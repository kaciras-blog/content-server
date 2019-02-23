package net.kaciras.blog.api.category;

import net.kaciras.blog.api.MapStructConfig;
import net.kaciras.blog.api.article.ArticleRepository;
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
		vo.setArticleCount(articleRepository.countByCategory(category.getId()));
		vo.setBanner(categoryManager.getBanner(category));
	}

	abstract void copyPropsInternal(@MappingTarget CategoryVo aggregation, Category category);

	@IterableMapping(qualifiedByName = "CategoryVo")
	abstract List<CategoryVo> categoryView(List<Category> list);

	abstract Category toCategory(CategoryAttributes viewObject);

	abstract void update(@MappingTarget Category category, CategoryAttributes attributes);
}
