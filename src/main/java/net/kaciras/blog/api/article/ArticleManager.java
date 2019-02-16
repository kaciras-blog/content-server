package net.kaciras.blog.api.article;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.event.category.CategoryRemovedEvent;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Service
public class ArticleManager {

	private final ClassifyDAO classifyDAO;
	private final ArticleRepository repository;
	private final MessageClient messageClient;

	@PostConstruct
	private void init() {
		messageClient.getChannel(CategoryRemovedEvent.class)
				.subscribe(event -> classifyDAO.updateCategory(event.getId(), event.getParent()));
	}
}
