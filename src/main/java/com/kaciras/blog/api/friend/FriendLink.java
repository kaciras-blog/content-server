package com.kaciras.blog.api.friend;

import com.kaciras.blog.infra.codec.ImageReference;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.time.Instant;

@AllArgsConstructor
public final class FriendLink {

	@NotNull
	public URI url;

	@Length(min = 1, max = 16)
	public String name;

	@NotNull
	public ImageReference favicon;

	@NotNull
	public ImageReference background;

	/** 用于验证对方是否加本站为友链的页面，是完整的URL，为null则不验证 */
	@Nullable
	public URI friendPage;

	/** 成为朋友的时间，在Controller里设置 */
	public Instant createTime;
}
