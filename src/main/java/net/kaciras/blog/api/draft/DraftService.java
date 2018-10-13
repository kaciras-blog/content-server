package net.kaciras.blog.api.draft;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.article.ArticleService;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;
import net.kaciras.blog.infrastructure.event.article.ArticleCreatedEvent;
import net.kaciras.blog.infrastructure.event.article.ArticleUpdatedEvent;
import net.kaciras.blog.infrastructure.message.MessageClient;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import net.kaciras.blog.infrastructure.principal.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

@RequireAuthorize
@RequiredArgsConstructor
@Service
public class DraftService {

	private final ArticleService articleService;
	private final DraftRepository draftRepository;
	private final DraftMapper draftMapper;

	private final MessageClient messageClient;

	@Value("${draft.delete-after-publish}")
	private boolean deleteAfterSubmit;

	@PostConstruct
	private void init() {
		messageClient.subscribe(ArticleCreatedEvent.class, event -> {
			if (deleteAfterSubmit) draftRepository.remove(event.getDraftId());
		});
		messageClient.subscribe(ArticleUpdatedEvent.class, event -> {
			if (deleteAfterSubmit) draftRepository.remove(event.getDraftId());
		});
	}

	public Draft get(int id) {
		return draftRepository.getById(id);
	}

	public List<Draft> getList(int userId) {
		return draftRepository.findByUser(userId);
	}

	@Transactional
	public int save(DraftSaveRequest dto) {
		var draft = draftRepository.getById(dto.getId());
		draft.addHistory(dto);
		return draft.getSaveCount() + 1;
	}

	public void deleteByUser(int userId) {
		draftRepository.clear(userId);
	}

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

	public int newDraft(Integer article) {
		var draft = article == null
				? defaultDraft()
				: draftMapper.fromArticle(articleService.getArticle(article));

		draft.setUserId(SecurityContext.getUserId());
		return draftRepository.add(draft);
	}

	public List<DraftHistory> getHistories(int id) {
		return draftRepository.getById(id).getHistories();
	}
}
