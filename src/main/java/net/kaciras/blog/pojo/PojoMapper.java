package net.kaciras.blog.pojo;

import net.kaciras.blog.domain.article.Article;
import net.kaciras.blog.domain.category.Category;
import net.kaciras.blog.domain.discuss.Discussion;
import net.kaciras.blog.domain.draft.DraftDTO;
import net.kaciras.blog.domain.draft.DraftHistory;
import net.kaciras.blog.domain.user.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PojoMapper {

	ArticlePreviewVo articlePreview(Article article);

	ArticleVo articleView(Article article);

	DraftVo draftView(DraftDTO draft);

	@Named("DraftHistory")
	DraftHistoryVo toDraftHistoryVO(DraftHistory draft);

	@IterableMapping(qualifiedByName = "DraftHistory")
	List<DraftHistoryVo> toDraftHistoryVOList(List<DraftHistory> histories);

	@Named("Draft")
	DraftPreviewVo toDraftPreviewVO(DraftDTO draft);

	@IterableMapping(qualifiedByName = "Draft")
	List<DraftPreviewVo> toDraftPreviewVOList(List<DraftDTO> histories);

	DiscussionVo discussionView(Discussion dto);

	@Named("Category")
	CategoryVo categoryView(Category category);

	@IterableMapping(qualifiedByName = "Category")
	List<CategoryVo> categoryView(List<Category> histories);

	Category toCategory(CategoryVo vo);

	UserVo toUserVo(User user);
}
