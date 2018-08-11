package net.kaciras.blog.facade.controller;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.article.ArticleService;
import net.kaciras.blog.facade.pojo.ArticlePreviewVO;
import net.kaciras.blog.facade.pojo.PojoMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/recommendations")
final class RecommendController {

	private final ArticleService articleService;
	private final PojoMapper pojoMapper;

	@GetMapping("/articles")
	public List<ArticlePreviewVO> hotArticles() {
		return articleService.getHots().stream().map(pojoMapper::toPreviewVo).collect(Collectors.toList());
	}
}
