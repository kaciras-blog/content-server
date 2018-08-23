package net.kaciras.blog.recommend;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.article.ArticlePreviewVo;
import net.kaciras.blog.article.ArticleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/recommendation")
final class RecommendController {

	private final ArticleService articleService;

	@GetMapping("/articles")
	public List<ArticlePreviewVo> hotArticles() {
		return articleService.getHotArticles();
	}
}
