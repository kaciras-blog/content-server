package com.kaciras.blog.api.article;

import com.kaciras.blog.infra.exception.ResourceDeletedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ArticleManager {

	private final ArticleRepository repository;

	/**
	 * 获取一个文章，但会检查文章的删除状态。
	 *
	 * @param id 文章ID
	 * @return 文章对象
	 * @throws ResourceDeletedException 如果文章被标记为删除
	 */
	public Article getLiveArticle(int id) {
		var article = repository.get(id);

		if (article.isDeleted()) {
			throw new ResourceDeletedException();
		}
		return article;
	}
}
