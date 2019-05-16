package net.kaciras.blog.api.discuss;

import org.apache.ibatis.jdbc.SQL;

@SuppressWarnings("unused")
public final class SqlProvider {

	public String select(DiscussionQuery query) {
		var sql = new SQL().SELECT("*").FROM("discussion");
		putFilters(sql, query);

		var pageable = query.getPageable();
		if (pageable == null) {
			return sql.toString();
		}
		return sql.toString() + String.format(" LIMIT %d,%d", pageable.getPageNumber(), pageable.getPageSize());
	}

	public String selectCount(DiscussionQuery query) {
		var sql = new SQL().SELECT("COUNT(*)").FROM("discussion");
		putFilters(sql, query);
		return sql.toString();
	}

	// 前三个都带索引，state暂时没有索引所以放最后
	private void putFilters(SQL sql, DiscussionQuery query) {
		if (query.getUserId() != null) {
			sql.WHERE("user_id = #{userId}");
		}
		if (query.getParent() != null) {
			sql.WHERE("parent = #{parent}");
		}
		if (query.getObjectId() != null) {
			sql.WHERE("object_id = #{objectId} AND type=#{type}");
		}
		// assert query.state != null
		sql.WHERE("state = #{state}");
	}
}
