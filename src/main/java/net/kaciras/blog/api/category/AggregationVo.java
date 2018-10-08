package net.kaciras.blog.api.category;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class AggregationVo extends CategoryVo {

	private ImageRefrence bestBackground;

	private CategoryVo parent;

	private List<CategoryVo> children;
}
