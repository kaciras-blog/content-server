package net.kaciras.blog.domain.draft;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.SecurtyContext;
import net.kaciras.blog.domain.article.ArticleDTO;
import net.kaciras.blog.domain.article.ArticleService;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DraftService {

	private final ArticleService articleService;
	private final DraftRepository draftRepository;
	private final DraftMapper draftMapper;

	public DraftDTO get(int id) {
		SecurtyContext.checkAccept("DraftService", "ACCESS_OTHER");
		return draftMapper.toDTO(draftRepository.getById(id));
	}

	public List<DraftDTO> findByUser(int userId) {
		SecurtyContext.checkAccept("DraftService", "ACCESS_OTHER");
		return draftMapper.toDTOList(draftRepository.findByUser(userId));
	}

	public void save(DraftSaveDTO dto) {
		SecurtyContext.checkAccept("DraftService", "MODIFY_OTHER");
		draftRepository.getById(dto.getId()).save(dto);
	}

	public void deleteByUser(int userId) {
		SecurtyContext.checkAccept("DraftService", "MODIFY_OTHER");
		draftRepository.clear(userId);
	}

	public void delete(int id) {
		SecurtyContext.checkAccept("DraftService", "MODIFY_OTHER");
		draftRepository.delete(id);
	}

	private Draft defaultDraft() {
		Draft newDraft = new Draft();
		newDraft.setTitle("");
		newDraft.setSummary("");
		newDraft.setKeywords("");
		newDraft.setContent("");
		newDraft.setCover("placeholder.png");
		return newDraft;
	}

	public int newDraft(Integer article) {
		SecurtyContext.checkAccept("DraftService", "MODIFY");
		Draft draft;

		if (article == null) {
			draft = defaultDraft();
		} else {
			ArticleDTO a = articleService.getArticle(article).blockingGet();
			if (a.getUserId() != SecurtyContext.getCurrentUser()) {
				SecurtyContext.checkAccept("DraftService", "MODIFY_OTHER");
			}
			draft = draftMapper.fromArticle(a);
		}

		draft.setUserId(SecurtyContext.getCurrentUser());
		return draftRepository.add(draft);
	}

	public List<DraftHistory> getHistories(int id) {
		return draftRepository.getById(id).getHistories();
	}
}
