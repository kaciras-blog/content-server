package net.kaciras.blog.domain.category;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Configuration("CategoryContextConfig")
public class ContextConfig {

	private final CategoryDAO categoryDAO;

	@PostConstruct
	private void init() {
		Category.dao = categoryDAO;
	}
}
