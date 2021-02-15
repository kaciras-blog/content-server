package com.kaciras.blog.api.discuss;

import com.google.common.base.CaseFormat;
import com.kaciras.blog.infra.exception.RequestArgumentException;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.data.domain.Pageable;

@SuppressWarnings("unused")
public final class SqlProvider {

	private final String insertSQL;

	// 字段一多就好麻烦啊，要不要换别的 ORM 库呢
	public SqlProvider() {
		var sql = new SQL().INSERT_INTO("discussion");

		for (var field : Discussion.class.getDeclaredFields()) {
			var name = field.getName();
			var column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
			sql.VALUES(column, "#{" + name + "}");
		}
		this.insertSQL = sql.toString();
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

	// 前三个都带索引，state 没有所以放最后
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
	 *
	 * TODO: MySQL 有个 ORDER BY + LIMIT 使用堆排序导致不稳定的问题，Mariadb 不知怎样，试了一下似乎没有。
	 *
	 * @see DiscussionRepository#findAll
	 */
	private void applyPageable(SQL sql, Pageable pageable) {
		var sort = pageable.getSort();
		if (sort.isSorted()) {
			var order = sort.iterator().next();
			var column = order.getProperty();

			switch (column) {
				case "id":
				case "nest_size":
					break;
				default:
					throw new RequestArgumentException("不支持的排序：" + column);
			}
			sql.ORDER_BY(column + " " + order.getDirection());
		}
		sql.OFFSET(pageable.getPageNumber()).LIMIT(pageable.getPageSize());
	}
}
