package net.kaciras.blog.domain.article;

import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class ArticleSqlProvider {

	private Set<String> allowFields = Set.of("create_time", "update_time", "view_count");
//	private Set<String> compartors = Set.of(">", "<", "=", ">=", "<=", "!=");

	public String selectPreview(ArticleListRequest query) {
		SQL sql = new SQL().SELECT("*").FROM("Article AS A");

		switch (query.getDeletion()) {
			case TRUE:
				sql.WHERE("deleted = 1");
				break;
			case FALSE:
				sql.WHERE("deleted = 0");
				break;
		}

		//TODO: coupling
		Integer category = query.getCategory();
		if (category != null && category > 0) {
			sql.JOIN("CategoryTree AS B ON A.category=B.descendant").WHERE("B.ancestor=#{category}");
		}

		String field = query.getSort();
		if (field != null) {
			if (!allowFields.contains(field))
				throw new IllegalArgumentException("错误的过滤字段:" + field);
			sql.ORDER_BY(field + " " + (query.isDesc() ? "DESC" : "ASC"));
		} else {
			sql.ORDER_BY("id DESC"); //默认按发布顺序倒序
		}

		return sql.toString() + String.format(" LIMIT %d,%d", query.getStart(), query.getCount());
	}

	@SuppressWarnings("Convert2MethodRef")
	public String countInCategories(List<Integer> arg0) {
		String cateFilter = arg0.stream()
				.map(id -> id.toString())
				.collect(Collectors.joining(",", "category in (", ")"));
		return new SQL().SELECT("COUNT(*)").FROM("Article")
				.WHERE("deleted=0")
				.WHERE(cateFilter).toString();
	}
}
