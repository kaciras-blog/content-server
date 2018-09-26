package net.kaciras.blog.api.draft;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public final class DraftSaveRequest extends DraftContentBase implements Serializable {

	private int id;
	private Integer articleId;
}