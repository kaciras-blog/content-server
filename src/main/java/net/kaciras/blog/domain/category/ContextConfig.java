package net.kaciras.blog.domain.category;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Configuration("CategoryContextConfig")
public class ContextConfig {

	private final CategoryDAO categoryDAO;
	private final MessageClient messageClient;

	@PostConstruct
	private void init() {
		Category.dao = categoryDAO;
		Category.messageClient = messageClient;
	}
}
