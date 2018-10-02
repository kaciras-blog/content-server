package net.kaciras.blog.api.article;

import net.kaciras.blog.infrastructure.CollectionUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.Set;

@SuppressWarnings("unused")
public final class SqlProvider {

	private Set<String> allowFields = Set.of("create_time", "update_time", "view_count");

	public String selectPreview(ArticleListRequest query) {
		var sql = new SQL().SELECT("*").FROM("article AS A");

		switch (query.getDeletion()) {
			case TRUE:
				sql.WHERE("deleted = 1");
				break;
			case FALSE:
				sql.WHERE("deleted = 0");
				break;
		}

		//TODO: coupling
		var category = query.getCategory();
		if (category != null && category > 0) {
			sql.JOIN("category_tree AS B ON A.category=B.descendant").WHERE("B.ancestor=#{category}");
		}

		var pageable = query.getPageable();
		var sort = pageable.getSort();

		if (sort.isUnsorted()) {
			sql.ORDER_BY("id DESC"); //默认按发布顺序倒序
		} else {
			var order = CollectionUtils.getFirst(sort);
			var p = order.getProperty();

			if (!allowFields.contains(p))
				throw new IllegalArgumentException("错误的过滤字段:" + p);
			sql.ORDER_BY(p + " " + order.getDirection());
		}

		return sql.toString() + String.format(" LIMIT %d,%d",
				pageable.getPageNumber(), Math.min(pageable.getPageSize(), 20)); // 限制最大结果数
	}

}
