package net.kaciras.blog.api.discuss;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kaciras.blog.api.ListSelectRequest;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public final class DiscussionQuery extends ListSelectRequest {

	boolean metaonly;

	private Integer objectId;
	private Integer type;

	private Integer userId;
	private Long parent;

	private DiscussionQuery(Integer objectId, Integer type, Integer userId, Long parent) {
		this.objectId = objectId;
		this.type = type;
		this.userId = userId;
		this.parent = parent;
	}

	// 检查下请求参数不能全为null
	boolean isInvalid() {
		return (objectId == null || type == null) && userId == null && parent == null;
	}

	public static DiscussionQuery byArticle(int article) {
		var r = new DiscussionQuery(article, 0, null, null);
		r.setMetaonly(true);
		return r; // TODO: optimize API
	}

	public static DiscussionQuery byParent(long parent) {
		return new DiscussionQuery(null, null, null, parent);
	}

	public static DiscussionQuery byUser(int userId) {
		return new DiscussionQuery(null, null, userId, null);
	}
}
