package com.kaciras.blog.api.misc;

import com.kaciras.blog.infra.codec.ImageReference;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

@ToString(of = "name")
@Getter
@Setter
public final class Card {

	@NotBlank
	private String name;

	@NotEmpty
	private String link;

	@NotNull
	private ImageReference picture;

	@Length(max = 100)
	private String description;
}
