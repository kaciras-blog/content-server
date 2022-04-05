package com.kaciras.blog.api.friend;

import com.kaciras.blog.infra.codec.ImageReference;
import com.kaciras.blog.infra.validate.HttpURI;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.time.Instant;

@AllArgsConstructor
public final class FriendLink {

	/** 对方网站的 URL，友链以它的域名作为ID */
	@NotNull
	@HttpURI
	public URI url;

	@NotNull
	@Length(min = 1, max = 16)
	public String name;

	/**
	 * 网站可能没有图标，此时前端可以显示个默认图。
	 */
	public ImageReference favicon;

	/**
	 * 背景必须要有，通常是对方网站的截图。
	 */
	@NotNull
	public ImageReference background;

	/**
	 * 用于验证对方是否加本站为友链的页面，null 则不验证。
	 */
	@HttpURI
	public URI friendPage;

	/**
	 * 成为朋友的时间，仅在服务端保存时设置。
	 */
	public Instant createTime;
}
