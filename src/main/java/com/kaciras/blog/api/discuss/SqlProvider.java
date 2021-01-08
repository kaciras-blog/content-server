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
		String[] fields = {"type", "parent", "floor", "nickname", "content", "time", "state", "address"};
		var placeholders = Arrays.stream(fields).map(f -> "#{" + f + "}").toArray(String[]::new);

		var sql = new SQL().INSERT_INTO("discussion");
		sql.INTO_COLUMNS(fields).INTO_VALUES(placeholders);
		this.insertSQL = sql
				.VALUES("object_id", "#{objectId}")
				.VALUES("user_id", "#{userId}")
				.VALUES("nest_id", "#{nestId}")
				.VALUES("tree_floor", "#{treeFloor}")
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
		if (query.getNestId() != null) {
			sql.WHERE("nest_id = #{nestId}");
		}
		sql.WHERE("state = #{state}"); // assert query.state != null
	}

	/**
	 * 向 SQL 语句中添加排序和分页，查询数量不使用该方法。
	 * <p>
	 * 排序字段已经在上层做了检查防注入。
	 *
	 * @see DiscussionRepository#findAll
	 */
	private void applyPageable(SQL sql, Pageable pageable) {
		var sort = pageable.getSort();
		if (sort.isSorted()) {
			var order = Misc.getFirst(sort);
			var column = order.getProperty();
			sql.ORDER_BY(column + " " + order.getDirection());
		}
		sql.OFFSET(pageable.getPageNumber()).LIMIT(pageable.getPageSize());
	}
}
