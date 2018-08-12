package net.kaciras.blog.domain.draft;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.event.article.ArticleCreatedEvent;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Configuration("DraftContextConfig")
class ContextConfig {

	private final MessageClient messageClient;
	private final DraftRepository draftRepository;
	private final DraftDAO draftDAO;

	@Value("${draft.delete-after-publish}")
	private boolean deleteAfterSubmit;

	@PostConstruct
	private void init() {
		messageClient.subscribe(ArticleCreatedEvent.class, event -> {
			if (deleteAfterSubmit) draftRepository.delete(event.getDraftId());
		});
	}

}
