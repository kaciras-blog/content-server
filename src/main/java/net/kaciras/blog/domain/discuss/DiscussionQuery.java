package net.kaciras.blog.domain.discuss;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.kaciras.blog.domain.ListSelectRequest;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public final class DiscussionQuery extends ListSelectRequest {

	boolean metaonly;

	private Integer postId;
	private Integer userId;
	private Integer parent;

	private DiscussionQuery(Integer postId, Integer userId, Integer parent) {
		this.postId = postId;
		this.userId = userId;
		this.parent = parent;
	}

	public static DiscussionQuery byPost(int postId) {
		return new DiscussionQuery(postId, null, null);
	}

	public static DiscussionQuery byParent(int parent) {
		return new DiscussionQuery(null, null, parent);
	}

	public static DiscussionQuery byUser(int userId) {
		return new DiscussionQuery(null, userId, null);
	}

}
