package net.kaciras.blog.api.article;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/recommendation")
class HotArticleController {

	private final ArticleRepository repository;
	private final ArticleMapper mapper;

	private List<ArticleVo> popular;

	@GetMapping("/articles")
	public List<ArticleVo> getHotArticles() {
		return popular;
	}

	// Scheduled 注解忽略访问修饰符
	@Scheduled(fixedDelay = 15 * 60 * 1000)
	private void updateHotsTask() {
		var request = new ArticleListQuery();
		request.setPageable(PageRequest.of(0, 6, Sort.Direction.DESC, "view_count"));
		popular = mapper.toViewObject(repository.findAll(request));
	}
}
