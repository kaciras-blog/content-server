package net.kaciras.blog.domain.draft;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
public class DraftHistory extends DraftContentBase implements Serializable {

	private int saveCount;
	private LocalDateTime time;
}
