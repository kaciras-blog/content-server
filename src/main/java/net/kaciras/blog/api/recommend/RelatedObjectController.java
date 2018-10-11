package net.kaciras.blog.api.recommend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recommendation/similar")
public class RelatedObjectController {

	@GetMapping("/{type}/{id}")
	public List<?> getSimilar(@PathVariable int type, @PathVariable int id) {
		return List.of();
	}
}
