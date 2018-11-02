package net.kaciras.blog.api.discuss;

import lombok.Data;

@Data
final class AddRequest {

	private int objectId;

	private int type;

	private String content;
}
