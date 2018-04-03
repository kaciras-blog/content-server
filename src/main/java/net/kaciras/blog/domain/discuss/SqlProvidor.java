package net.kaciras.blog.domain.discuss;

import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import org.apache.ibatis.jdbc.SQL;

import java.util.Set;

public final class SqlProvidor {

	private Set<String> sortFields = Set.of("time", "vote");

	public String select(DiscussionQuery query) {
		if (query.getCount() > 30) {
			throw new RequestArgumentException();
		}
		SQL sql = new SQL().SELECT("*").FROM("Discussion");

		putFilters(sql, query);

		String s = query.getSort();
		if (s != null && sortFields.contains(s)) {
			sql.ORDER_BY(s + " " + (query.isDesc() ? "DESC" : "ASC"));
		}

		return sql.toString() + String.format(" LIMIT %d,%d", query.getStart(), query.getCount());
	}

	public String selectCount(DiscussionQuery query) {
		SQL sql = new SQL().SELECT("COUNT(*)").FROM("Discussion");
		putFilters(sql, query);
		return sql.toString();
	}

	private void putFilters(SQL sql, DiscussionQuery query) {
		if (query.getParent() != null) {
			sql.WHERE("parent = #{parent}");
		} else {
			if (query.getPostId() != null) {
				sql.WHERE("post = #{articleId}");
			}
			if (query.getUserId() != null) {
				sql.WHERE("user = #{userId}");
			}
		}
		switch (query.getDeletedState()) {
			case FALSE:
				sql.WHERE("deleted = 0");
				break;
			case TRUE:
				sql.WHERE("deleted = 1");
				break;
		}
	}

}
