package net.kaciras.blog.domain.article;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.message.MessageClient;
import net.kaciras.blog.infrastructure.message.event.CategoryRemovedEvent;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Configuration
class ArticleConfig {

	private final ArticleDAO articleDAO;
	private final MessageClient messageClient;
	private final ClassifyDAO classifyDAO;

	@PostConstruct
	void init() {
		Article.articleDAO = articleDAO;
		//删除分类后将原分类下的文章移动到其父类中
		messageClient.subscribe(CategoryRemovedEvent.class, event -> classifyDAO.updateByCategory(event.getId(), event.getParent()));
	}
}
