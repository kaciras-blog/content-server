package net.kaciras.blog.draft;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public final class DraftSaveDTO extends DraftContentBase implements Serializable {

	private int id;
	private Integer articleId;
}
