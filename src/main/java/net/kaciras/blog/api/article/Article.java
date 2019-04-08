package net.kaciras.blog.api.article;

import lombok.*;
import net.kaciras.blog.infrastructure.exception.ResourceDeletedException;
import net.kaciras.blog.infrastructure.exception.ResourceStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

@EqualsAndHashCode(of = "id", callSuper = false)
@ToString(of = "id")
@Data
@Configurable
public final class Article extends ArticleContentBase {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ArticleDAO articleDAO;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private ClassifyDAO classifyDAO;

// - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private int id;
	private int category;

	/**
	 * 显示在URL中的标题，有利于SEO，不要出现与URL中的特殊字符。
	 * 为什么要单独定义此字段而不是使用原始标题：
	 *     1.原始标题中如果存在URL关键字，如 `?`,`/` 等，需要转换或删除。
	 *     2.原始标题能够被修改，而URL经常更改不利于SEO。
	 *     3.部分搜索引擎对非ASCII字符不友好，可能需要将标题转换成其它形式，比如英语。
	 */
	private String urlTitle;

	private LocalDateTime create;
	private LocalDateTime update;
	private int viewCount;
	private boolean deleted;

	public void recordView() {
		articleDAO.increaseViewCount(getId());
	}

	/**
	 * 更改文章的删除状态，如果操作没有意义（当前状态与目标状态相同）则抛出异常。
	 *
	 * @param value 目标状态，true表示删除，false表示没有删除。
	 */
	public void updateDeleted(boolean value) {
		if (deleted == value) {
			if (deleted) {
				throw new ResourceDeletedException("文章已经删除了");
			}
			throw new ResourceStateException("文章还没有被删除呢");
		}
		articleDAO.updateDeleted(id, value);
	}

	public void updateUrlTitle(@NonNull String urlTitle) {
		articleDAO.updateUrlTitle(id, urlTitle);
	}

	public void updateCategory(int category) {
		classifyDAO.updateByArticle(id, category);
	}

	@Nullable
	public ArticleLink getPreviousLink() {
		return articleDAO.getNeighbor(id, "<");
	}

	@Nullable
	public ArticleLink getNextLink() {
		return articleDAO.getNeighbor(id, ">");
	}
}
