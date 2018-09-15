package net.kaciras.blog.api.draft;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.Authenticator;
import net.kaciras.blog.api.AuthenticatorFactory;
import net.kaciras.blog.api.SecurtyContext;
import net.kaciras.blog.api.article.ArticleService;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;
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

	public List<DraftDTO> getList(int userId) {
		authenticator.require("POWER_MODIFY");
		return draftMapper.toDTOList(draftRepository.findByUser(userId));
	}

	public void save(DraftSaveDTO dto) {
		authenticator.require("POWER_MODIFY");
		draftRepository.getById(dto.getId()).save(dto);
	}

	public int saveNewHistory(DraftSaveDTO dto) {
		authenticator.require("POWER_MODIFY");
		var draft = draftRepository.getById(dto.getId());
		draft.saveNewHistory(dto);
		return draft.getSaveCount() + 1;
	}

	public void deleteByUser(int userId) {
		authenticator.require("POWER_MODIFY");
		draftRepository.clear(userId);
	}

	public void delete(int id) {
		authenticator.require("POWER_MODIFY");
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

	public int newDraft(Integer article) {
		authenticator.require("USE");

		var draft = article == null
				? defaultDraft()
				: draftMapper.fromArticle(articleService.getArticle(article));

		draft.setUserId(SecurtyContext.getCurrentUser());
		return draftRepository.add(draft);
	}

	public List<DraftHistory> getHistories(int id) {
		var draft = draftRepository.getById(id);
		if (SecurtyContext.isNotUser(draft.getUserId())) {
			authenticator.require("POWER_MODIFY");
		}
		return draft.getHistories();
	}
}
