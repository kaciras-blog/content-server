package net.kaciras.blog.api.article;

import net.kaciras.blog.infrastructure.Misc;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.data.domain.Sort;

import java.util.Set;

@SuppressWarnings("unused")
public final class SqlProvider {

	private Set<String> allowFields = Set.of("create_time", "update_time", "view_count");

	private void applyFilters(SQL sql, ArticleListQuery query) {
		switch (query.getDeletion()) {
			case TRUE:
				sql.WHERE("deleted = 1");
				break;
			case FALSE:
				sql.WHERE("deleted = 0");
				break;
		}

		var category = query.getCategory();
		if (category > 0) {
			if (query.isRecursive()) {
				//TODO: coupling 耦合
				sql.JOIN("category_tree AS B ON A.category=B.descendant")
						.WHERE("B.ancestor=#{category}");
			} else {
				sql.WHERE("A.category = #{category}");
			}
		}
	}

	private void applySorts(SQL sql, Sort sort) {
		if (sort.isUnsorted()) {
			sql.ORDER_BY("id DESC"); // 默认按发布顺序倒序
		} else {
			var order = Misc.getFirst(sort);
			var p = order.getProperty();

			if (!allowFields.contains(p)) {
				throw new IllegalArgumentException("错误的过滤字段:" + p);
			}
			sql.ORDER_BY(p + " " + order.getDirection());
		}
	}

	public String selectPreview(ArticleListQuery query) {
		var sql = new SQL().SELECT("*").FROM("article AS A");
		var pageable = query.getPageable();

		applyFilters(sql, query);
		applySorts(sql, pageable.getSort());

		return sql.toString() + String.format(" LIMIT %d,%d",
				pageable.getPageNumber(), Math.min(pageable.getPageSize(), 20));
	}

	public String selectCount(ArticleListQuery query) {
		var sql = new SQL().SELECT("COUNT(*)").FROM("article AS A");
		applyFilters(sql, query);
		return sql.toString();
	}
}
