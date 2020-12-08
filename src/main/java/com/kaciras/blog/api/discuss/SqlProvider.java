package com.kaciras.blog.api.discuss;

import com.kaciras.blog.infra.Misc;
import com.kaciras.blog.infra.exception.RequestArgumentException;
import org.apache.ibatis.jdbc.SQL;

@SuppressWarnings("unused")
public final class SqlProvider {

	public String select(DiscussionQuery query) {

		// 因为回复数不多就不加冗余字段里，直接 COUNT 统计。
		var sql = new SQL()
				.SELECT("*, COUNT(*) AS reply")
				.FROM("discussion")
				.JOIN("(SELECT parent AS _p FROM discussion) AS B ON B._p=id")
				.GROUP_BY("id");

		applyFilters(sql, query);

		var pageable = query.getPageable();
		if (pageable == null) {
			return sql.toString();
		}

		if (pageable.getSort().isSorted()) {
			var order = Misc.getFirst(pageable.getSort());
			var column = order.getProperty();
			sql.ORDER_BY(column + " " + order.getDirection());

			switch (column) {
				case "reply", "id", "score" -> sql.ORDER_BY(column + " " + order.getDirection());
				default -> throw new RequestArgumentException("不支持的排序：" + column);
			}
		}

		return sql.OFFSET(pageable.getPageNumber()).LIMIT(pageable.getPageSize()).toString();
	}

	public String selectCount(DiscussionQuery query) {
		var sql = new SQL().SELECT("COUNT(*)").FROM("discussion AS A");
		applyFilters(sql, query);
		return sql.toString();
	}

	// 前三个都带索引，state 暂时没有索引所以放最后
	private void applyFilters(SQL sql, DiscussionQuery query) {
		if (query.getType() != null) {
			sql.WHERE("type = #{type}");
		}
		if (query.getObjectId() != null) {
			if (query.getType() == null) {
				throw new RequestArgumentException("查询参数中有 objectId 的情况下必须指定 type");
			}
			sql.WHERE("object_id = #{objectId}");
		}
		if (query.getParent() != null) {
			sql.WHERE("parent = #{parent}");
		}
		if (query.getUserId() != null) {
			sql.WHERE("user_id = #{userId}");
		}
		sql.WHERE("state = #{state}"); // assert query.state != null
	}
}
