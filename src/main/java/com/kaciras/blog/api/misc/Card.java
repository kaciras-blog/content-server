package com.kaciras.blog.api.misc;

import com.kaciras.blog.infra.codec.ImageReference;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ToString(of = "name")
@Getter
@Setter
public final class Card {

	@Length(max = 20)
	private String name;

	@NotEmpty
	private String link;

	@NotNull
	private ImageReference picture;

	@Length(max = 100)
	private String description;
}
