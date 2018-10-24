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
}
