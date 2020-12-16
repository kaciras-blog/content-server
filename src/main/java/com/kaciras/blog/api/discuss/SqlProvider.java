package com.kaciras.blog.api.discuss;

import com.kaciras.blog.infra.Misc;
import com.kaciras.blog.infra.exception.RequestArgumentException;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;

@SuppressWarnings("unused")
public final class SqlProvider {

	private final String insertSQL;

	// 字段一多就好麻烦啊，要不要换别的 ORM 库呢
	public SqlProvider() {
		String[] fields = {"type", "parent", "nickname", "content", "time", "state", "address"};
		var placeholders = Arrays.stream(fields).map(f -> "#{" + f + "}").toArray(String[]::new);

		var sql = new SQL().INSERT_INTO("discussion");
		sql.INTO_COLUMNS(fields).INTO_VALUES(placeholders);
		this.insertSQL = sql
				.VALUES("object_id", "#{objectId}")
				.VALUES("user_id", "#{userId}")
				.VALUES("reply_floor", "#{replyFloor}")
				.VALUES("channel_floor", "#{channelFloor}")
				.toString();
	}

	public String insert(Discussion value) {
		return insertSQL;
	}

	public String select(DiscussionQuery query) {
		var sql = new SQL().SELECT("*").FROM("discussion");
		applyFilters(sql, query);

		var pageable = query.getPageable();
		if (pageable != null) {
			applyPageable(sql, pageable);
		}
		return sql.toString();
	}

	public String selectCount(DiscussionQuery query) {
		var sql = new SQL().SELECT("COUNT(*)").FROM("discussion");
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

	private void applyPageable(SQL sql, Pageable pageable) {
		if (pageable.getSort().isSorted()) {
			var order = Misc.getFirst(pageable.getSort());
			var column = order.getProperty();
			sql.ORDER_BY(column + " " + order.getDirection());

			switch (column) {
				case "reply", "id" -> sql.ORDER_BY(column + " " + order.getDirection());
				default -> throw new RequestArgumentException("不支持的排序：" + column);
			}
		}
		sql.OFFSET(pageable.getPageNumber()).LIMIT(pageable.getPageSize());
	}
}
