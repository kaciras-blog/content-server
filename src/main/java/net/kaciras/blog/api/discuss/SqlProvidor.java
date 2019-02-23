package net.kaciras.blog.api.discuss;

import org.apache.ibatis.jdbc.SQL;

@SuppressWarnings("unused")
public final class SqlProvidor {

	public String select(DiscussionQuery query) {
		var sql = new SQL().SELECT("*").FROM("discussion");
		putFilters(sql, query);

		var pageable = query.getPageable();
		return sql.toString() + String.format(" LIMIT %d,%d", pageable.getPageNumber(), pageable.getPageSize());
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
			if (query.getObjectId() != null && query.getType() != null) {
				sql.WHERE("object_id = #{objectId} AND `type` = #{type}");
			}
			if (query.getUserId() != null) {
				sql.WHERE("user_id = #{userId}");
			}
			if(!query.isMetaonly()) {
				sql.WHERE("parent = 0"); // 文章列表查询数量时包含楼中楼
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
