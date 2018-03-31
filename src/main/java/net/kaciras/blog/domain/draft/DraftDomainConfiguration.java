package net.kaciras.blog.domain.draft;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.message.MessageClient;
import net.kaciras.blog.infrastructure.message.event.ArticleCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Configuration("DraftContextConfig")
public class DraftDomainConfiguration {

	private final MessageClient messageClient;
	private final DraftRepository draftRepository;
	private final DraftDAO draftDAO;

	@Value("${draft.deleteAfterSubmit}")
	private boolean deleteAfterSubmit;

	@PostConstruct
	public void init() {
		messageClient.subscribe(ArticleCreatedEvent.class, event -> {
			if (deleteAfterSubmit) draftRepository.delete(event.getDraftId());
		});
	}

}
