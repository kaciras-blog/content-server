package com.kaciras.blog.api.category;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 聚合视图，除了分类本身的属性之外还包含了子分类。
 */
@Getter
@Setter
final class AggregationVo extends CategoryVo {

	private List<CategoryVo> children;
}
