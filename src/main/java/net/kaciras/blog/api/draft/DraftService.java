package net.kaciras.blog.api.draft;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.RequirePrincipal;
import net.kaciras.blog.api.SecurtyContext;
import net.kaciras.blog.api.article.ArticleService;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DraftService {

	private final ArticleService articleService;
	private final DraftRepository draftRepository;
	private final DraftMapper draftMapper;

	@RequirePrincipal
	public Draft get(int id) {
		return draftRepository.getById(id);
	}

	@RequirePrincipal
	public List<Draft> getList(int userId) {
		return draftRepository.findByUser(userId);
	}

	@RequirePrincipal
	@Transactional
	public int save(DraftSaveRequest dto) {
		var draft = draftRepository.getById(dto.getId());
		draft.addHistory(dto);
		return draft.getSaveCount() + 1;
	}

	@RequirePrincipal
	public void deleteByUser(int userId) {
		draftRepository.clear(userId);
	}

	@RequirePrincipal
	public void delete(int id) {
		draftRepository.remove(id);
	}

	private Draft defaultDraft() {
		var newDraft = new Draft();
		newDraft.setTitle("");
		newDraft.setSummary("");
		newDraft.setKeywords("");
		newDraft.setContent("");
		newDraft.setCover(ImageRefrence.parse("placeholder.png"));
		return newDraft;
	}

	@RequirePrincipal
	public int newDraft(Integer article) {
		var draft = article == null
				? defaultDraft()
				: draftMapper.fromArticle(articleService.getArticle(article));

		draft.setUserId(SecurtyContext.getUserId());
		return draftRepository.add(draft);
	}

	@RequirePrincipal
	public List<DraftHistory> getHistories(int id) {
		return draftRepository.getById(id).getHistories();
	}
}
