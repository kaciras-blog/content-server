package net.kaciras.blog.api.recommend;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.article.ArticleListQuery;
import net.kaciras.blog.api.article.ArticleService;
import net.kaciras.blog.api.article.PreviewVo;
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
class RecArticleController {

	private final ArticleService articleService;

	private List<PreviewVo> popular;

	@GetMapping("/articles")
	public List<PreviewVo> getHotArticles() {
		return popular;
	}

	@Scheduled(fixedDelay = 5 * 60 * 1000)
	void updateHotsTask() {
		var request = new ArticleListQuery();
		request.setPageable(PageRequest.of(0, 6, Sort.Direction.DESC, "view_count"));
		popular = articleService.getList(request);
	}
}
