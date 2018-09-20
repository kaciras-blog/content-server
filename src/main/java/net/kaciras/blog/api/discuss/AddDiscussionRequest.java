package net.kaciras.blog.api.discuss;

import lombok.Data;

@Data
public final class AddDiscussionRequest {

	private int objectId;

	private int type;

	private String content;
}
