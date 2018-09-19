package net.kaciras.blog.api.discuss;

import org.apache.ibatis.jdbc.SQL;

import java.util.Set;

public final class SqlProvidor {

	private Set<String> sortFields = Set.of("time", "vote");

	public String select(DiscussionQuery query) {
		var sql = new SQL().SELECT("*").FROM("discussion");

		putFilters(sql, query);

		var s = query.getSort();
		if (s != null && sortFields.contains(s)) {
			sql.ORDER_BY(s + " " + (query.isDesc() ? "DESC" : "ASC"));
		}

		return sql.toString() + String.format(" LIMIT %d,%d", query.getStart(), query.getCount());
	}

	public String selectCount(DiscussionQuery query) {
		var sql = new SQL().SELECT("COUNT(*)").FROM("discussion");
		putFilters(sql, query);
		return sql.toString();
	}

	private void putFilters(SQL sql, DiscussionQuery query) {
		if (query.getParent() != null) {
			sql.WHERE("parent = #{parent}");
		} else {
			if (query.getArticleId() != null) {
				sql.WHERE("post = #{articleId}");
			}
			if (query.getUserId() != null) {
				sql.WHERE("user = #{userId}");
			}
		}
		switch (query.getDeletion()) {
			case FALSE:
				sql.WHERE("deleted = 0");
				break;
			case TRUE:
				sql.WHERE("deleted = 1");
				break;
		}
	}

}
