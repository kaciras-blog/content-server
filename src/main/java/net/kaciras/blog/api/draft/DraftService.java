package net.kaciras.blog.api.draft;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.article.ArticleService;
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
	private final DraftMapper mapper;

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
	public int saveNew(DraftSaveRequest dto) {
		return draftRepository.getById(dto.getId()).getHistoryList().add(dto);
	}

	public void save(DraftSaveRequest request) {
		var draft = draftRepository.getById(request.getId());
		draft.getHistoryList().update(request);
	}

	public void deleteByUser(int userId) {
		draftRepository.clear(userId);
	}

	public void delete(int id) {
		draftRepository.remove(id);
	}

	@Transactional
	public int newDraft(Integer article) {
		var draft = new Draft();
		draft.setUserId(SecurityContext.getUserId());
		draft.setArticleId(article);
		draftRepository.add(draft);

		var content = article == null ? DraftContent.initial()
				: mapper.fromArticle(articleService.getArticle(article));
		draft.getHistoryList().add(content);
		return draft.getId();
	}

	public List<DraftHistory> getHistories(int id) {
		return draftRepository.getById(id).getHistoryList().findAll();
	}

	public DraftHistory getHistory(int id, int saveCount) {
		return draftRepository.getById(id).getHistoryList().findBySaveCount(saveCount);
	}
}
