package net.kaciras.blog.pojo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class DraftVO extends DraftPreviewVO {

	private String keywords;
	private String content;
}
