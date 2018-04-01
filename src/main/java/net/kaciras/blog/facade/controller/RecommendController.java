package net.kaciras.blog.facade.controller;

import io.reactivex.Observable;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.article.ArticleService;
import net.kaciras.blog.facade.pojo.ArticlePreviewVO;
import net.kaciras.blog.facade.pojo.PojoMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/recommendations")
public final class RecommendController {

	private final ArticleService articleService;
	private final PojoMapper pojoMapper;

	@GetMapping("/articles")
	public Observable<ArticlePreviewVO> hotArticles() {
		return articleService.getHots().map(pojoMapper::toPreviewVo);
	}
}
