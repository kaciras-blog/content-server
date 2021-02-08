package com.kaciras.blog.api.draft;

import java.time.Instant;

public final class DraftVO {

	public int id;
	public Integer articleId;
	public int userId;

	public String title;
	public int lastSaveCount;

	public Instant createTime;
	public Instant updateTime;
}
