package com.kaciras.blog.api.friend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.kaciras.blog.infra.codec.ImageReference;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@AllArgsConstructor(onConstructor_ = @JsonCreator)
@Setter
public final class FriendLink {

	@Length(min = 1, max = 16)
	public String name;

	@NotEmpty
	public String url;

	@NotNull
	public ImageReference background;

	@NotNull
	public ImageReference favicon;

	/**
	 * 用于验证对方是否加本站为友链的页面，如果为null则不验证。
	 */
	@Nullable
	public String friendPage;

	public Instant createTime;
}
