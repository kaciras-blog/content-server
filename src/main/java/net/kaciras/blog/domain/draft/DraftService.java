package net.kaciras.blog.domain.draft;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.SecurtyContext;
import net.kaciras.blog.domain.article.ArticleService;
import net.kaciras.blog.domain.permission.Authenticator;
import net.kaciras.blog.domain.permission.AuthenticatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DraftService {

	private final ArticleService articleService;
	private final DraftRepository draftRepository;
	private final DraftMapper draftMapper;

	private Authenticator authenticator;

	@Autowired
	public void setAuthenticator(AuthenticatorFactory factory) {
		this.authenticator = factory.create("DRAFT");
	}

	public DraftDTO get(int id) {
		authenticator.require("POWER_MODIFY");
		return draftMapper.toDTO(draftRepository.getById(id));
	}

	public List<DraftDTO> findByUser(int userId) {
		authenticator.require("POWER_MODIFY");
		return draftMapper.toDTOList(draftRepository.findByUser(userId));
	}

	public void save(DraftSaveDTO dto) {
		authenticator.require("POWER_MODIFY");
		draftRepository.getById(dto.getId()).save(dto);
	}

	public void deleteByUser(int userId) {
		authenticator.require("POWER_MODIFY");
		draftRepository.clear(userId);
	}

	public void delete(int id) {
		authenticator.require("POWER_MODIFY");
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
		authenticator.require("USE");
		Draft draft;

		if (article == null) {
			draft = defaultDraft();
		} else {
			draft = draftMapper.fromArticle(articleService.getArticle(article).blockingGet());
		}

		draft.setUserId(SecurtyContext.getCurrentUser());
		return draftRepository.add(draft);
	}

	public List<DraftHistory> getHistories(int id) {
		Draft draft = draftRepository.getById(id);
		if(SecurtyContext.isNotUser(draft.getUserId())) {
			authenticator.require("POWER_MODIFY");
		}
		return draft.getHistories();
	}
}
