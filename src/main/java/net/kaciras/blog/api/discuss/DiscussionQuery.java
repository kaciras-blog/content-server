package net.kaciras.blog.api.discuss;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kaciras.blog.api.ListSelectRequest;

@EqualsAndHashCode(callSuper = true)
@Data
public final class DiscussionQuery extends ListSelectRequest {

	boolean metaonly;

	private Integer articleId;
	private Integer userId;
	private Integer parent;

	public DiscussionQuery() {}

	private DiscussionQuery(Integer articleId, Integer userId, Integer parent) {
		this.articleId = articleId;
		this.userId = userId;
		this.parent = parent;
	}

	public static DiscussionQuery byArticle(int article) {
		return new DiscussionQuery(article, null, null);
	}

	public static DiscussionQuery byParent(int parent) {
		return new DiscussionQuery(null, null, parent);
	}

	public static DiscussionQuery byUser(int userId) {
		return new DiscussionQuery(null, userId, null);
	}

}
