package com.kaciras.blog.api.category;

import java.util.List;

/**
 * 聚合视图，除了分类本身的属性之外还包含了子分类。
 */
final class AggregationVO extends CategoryVO {

	public List<CategoryVO> children;
}
