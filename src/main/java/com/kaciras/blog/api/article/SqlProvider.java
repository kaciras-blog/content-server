package com.kaciras.blog.api.article;

import org.apache.ibatis.jdbc.SQL;
import org.springframework.data.domain.Sort;

import java.util.Set;

@SuppressWarnings("unused")
public final class SqlProvider {

	private final Set<String> allowFields = Set.of("id", "create_time", "update_time", "view_count");

	/*
	 * 【关于SQL涉及category表】
	 * 这是可以的，并不算增加了耦合程度。首先对于领域层来说，存储的实现是抽象的，根本就不关心SQL的问题。
	 * 而对于下层，我又不搞什么分布式放一起查询完全没问题，真要分离的话就做缓存解决。
	 */
	private void applyFilters(SQL sql, ArticleListQuery query) {
		switch (query.getDeletion()) {
			case ALIVE -> sql.WHERE("deleted = 0");
			case DELETED -> sql.WHERE("deleted = 1");
		}

		if (query.getCategory() > 0) {
			if (query.isRecursive()) {
				sql.JOIN("category_tree AS B ON A.category=B.descendant").WHERE("B.ancestor=#{category}");
			} else {
				sql.WHERE("A.category = #{category}");
			}
		}
	}

	private void applySorts(SQL sql, Sort sort) {
		if (sort.isUnsorted()) {
			sql.ORDER_BY("id DESC"); // 默认按发布顺序倒序
		} else {
			var order = sort.iterator().next();
			var p = order.getProperty();

			if (!allowFields.contains(p)) {
				throw new IllegalArgumentException("错误的过滤字段: " + p);
			}
			sql.ORDER_BY(p + " " + order.getDirection());
		}
	}

	public String selectPreview(ArticleListQuery query) {
		var sql = new SQL().SELECT("*").FROM("article AS A");
		var pageable = query.getPageable();

		applyFilters(sql, query);
		applySorts(sql, pageable.getSort());

		// Mariadb 的 OFFSET-LIMIT 语句必须是 LIMIT 在前，另外也能用 LIMIT x,x 的形式
		return sql.OFFSET(pageable.getPageNumber()).LIMIT(pageable.getPageSize()).toString();
	}

	public String selectCount(ArticleListQuery query) {
		var sql = new SQL().SELECT("COUNT(*)").FROM("article AS A");
		applyFilters(sql, query);
		return sql.toString();
	}
}
