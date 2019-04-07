package net.kaciras.blog.api.article;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.kaciras.blog.infrastructure.codec.ImageReference;

// 虽然在分类领域中也可能需要类似的视图，但不应看做是同一对象
@ToString(of = {"id", "name"})
@Getter
@Setter
public final class SimpleCategoryVo {

	private int id;
	private ImageReference cover;
	private String name;
}
