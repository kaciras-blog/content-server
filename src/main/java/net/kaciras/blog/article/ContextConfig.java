package net.kaciras.blog.article;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.event.category.CategoryRemovedEvent;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Configuration("ArticleContextConfig")
class ContextConfig {

	private final MessageClient messageClient;
	private final ClassifyDAO classifyDAO;

	@PostConstruct
	private void init() {
		messageClient.subscribe(CategoryRemovedEvent.class, this::categoryRemoved);
	}

	/** 删除分类后将原分类下的文章移动到其父类中 */
	private void categoryRemoved(CategoryRemovedEvent event) {
		classifyDAO.updateCategory(event.getId(), event.getParent());
	}
}
