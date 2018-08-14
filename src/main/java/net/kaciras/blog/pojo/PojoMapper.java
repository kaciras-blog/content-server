package net.kaciras.blog.pojo;

import net.kaciras.blog.domain.article.Article;
import net.kaciras.blog.domain.category.Category;
import net.kaciras.blog.domain.discuss.Discussion;
import net.kaciras.blog.domain.draft.DraftDTO;
import net.kaciras.blog.domain.draft.DraftHistory;
import net.kaciras.blog.domain.user.User;
import net.kaciras.blog.pojo.*;
import org.mapstruct.*;

import java.util.List;

@MapperConfig(unmappedTargetPolicy = ReportingPolicy.IGNORE)
@Mapper(componentModel = "spring")
public interface PojoMapper {

	ArticlePreviewVO toPreviewVo(Article article);

	ArticleVO toVO(Article article);

	DraftVO toDraftVO(DraftDTO draft);

	@Named("B")
	DraftHistoryVO toDraftHistoryVO(DraftHistory draft);

	@IterableMapping(qualifiedByName = "B")
	List<DraftHistoryVO> toDraftHistoryVOList(List<DraftHistory> histories);

	@Named("C")
	DraftPreviewVO toDraftPreviewVO(DraftDTO draft);

	@IterableMapping(qualifiedByName = "C")
	List<DraftPreviewVO> toDraftPreviewVOList(List<DraftDTO> histories);

	DiscussionVO toDiscussionVO(Discussion dto);

	@Named("Category")
	CategoryVO toCategoryVO(Category category);

	@IterableMapping(qualifiedByName = "Category")
	List<CategoryVO> toCategoryVOList(List<Category> histories);

	Category toCategory(CategoryVO vo);

	UserVO toUserVo(User user);
}
