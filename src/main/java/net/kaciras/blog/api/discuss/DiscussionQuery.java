package net.kaciras.blog.api.discuss;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kaciras.blog.api.ListSelectRequest;

@EqualsAndHashCode(callSuper = true)
@Data
public final class DiscussionQuery extends ListSelectRequest {

	boolean metaonly;

	private Integer objectId;
	private Integer type;

	private Integer userId;
	private Integer parent;

	public DiscussionQuery() {}

	private DiscussionQuery(Integer objectId, Integer type, Integer userId, Integer parent) {
		this.objectId = objectId;
		this.userId = userId;
		this.parent = parent;
		this.type = type;
	}

	public static DiscussionQuery byArticle(int article) {
		return new DiscussionQuery(article, 0, null, null);
	}

	public static DiscussionQuery byParent(int parent) {
		return new DiscussionQuery(null, null, null, parent);
	}

	public static DiscussionQuery byUser(int userId) {
		return new DiscussionQuery(null, null, userId, null);
	}

}
