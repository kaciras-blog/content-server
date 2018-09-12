package net.kaciras.blog.api.recommend;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.article.ArticleService;
import net.kaciras.blog.api.article.PreviewVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/recommendation")
final class RecArticleController {

	private final ArticleService articleService;

	@GetMapping("/articles")
	public List<PreviewVo> hotArticles() {
		return articleService.getHotArticles();
	}
}
