package com.kaciras.blog.api.user;

import com.kaciras.blog.api.account.AuthType;
import com.kaciras.blog.infra.codec.ImageReference;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public final class UserVo {

	private int id;

	private String name;
	private ImageReference avatar;

	private AuthType auth;

	private Instant createTime;

	// IP 地址仅用于批量处理，不在前端显示
}
