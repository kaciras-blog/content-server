package net.kaciras.blog.draft;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
public class DraftDTO extends DraftContentBase implements Serializable {

	private int id;
	private Integer articleId;
	private int userId;
	private LocalDateTime time;
}
