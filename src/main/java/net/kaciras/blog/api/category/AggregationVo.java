package net.kaciras.blog.api.category;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class AggregationVo extends CategoryVo {

	private Banner banner;

	private CategoryVo parent;

	private List<CategoryVo> children;
}
