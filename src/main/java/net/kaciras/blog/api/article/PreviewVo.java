package net.kaciras.blog.api.article;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.kaciras.blog.infra.codec.ImageReference;

import java.time.Instant;
import java.util.List;

// TODO: 跟ArticleVo越来越像了
@ToString(of = {"id", "title"})
@Getter
@Setter
public final class PreviewVo {

	private int id;
	private String urlTitle;

	private String title;
	private ImageReference cover;
	private List<String> keywords;
	private String summary;

	private String content;

	private Instant create;
	private Instant update;

	private int viewCount;
	private int discussionCount;
	private boolean deleted;

	private List<SimpleCategoryVo> categories;
}
