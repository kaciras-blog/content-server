package net.kaciras.blog.api.draft;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.event.article.ArticleCreatedEvent;
import net.kaciras.blog.infrastructure.event.article.ArticleUpdatedEvent;
import net.kaciras.blog.infrastructure.message.MessageClient;
import net.kaciras.blog.infrastructure.principal.RequireAuthorize;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@RequireAuthorize
@RequiredArgsConstructor
@Service
public class DraftManager {

	private final DraftRepository draftRepository;
	private final MessageClient messageClient;

	@Value("${draft.delete-after-publish}")
	private boolean deleteAfterSubmit;

	@PostConstruct
	private void init() {
		messageClient.subscribe(ArticleCreatedEvent.class)
				.filter(event -> deleteAfterSubmit)
				.subscribe(event -> draftRepository.remove(event.getDraftId()));
		messageClient.subscribe(ArticleUpdatedEvent.class)
				.filter(event -> deleteAfterSubmit)
				.subscribe(event -> draftRepository.remove(event.getDraftId()));
	}
}
