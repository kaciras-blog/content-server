package net.kaciras.blog.api.category;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AggregationVo extends CategoryVo {

	private Banner banner;

	private CategoryVo parent;

	private List<CategoryVo> children;
}
