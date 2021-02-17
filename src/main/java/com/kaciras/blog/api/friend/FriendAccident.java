package com.kaciras.blog.api.friend;

import com.kaciras.blog.api.notice.Activity;
import com.kaciras.blog.api.notice.ActivityType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

import java.net.URI;

/**
 * 友链相关的通知，用 Accident 是不是不太好……
 * <p>
 * 错误的友链多放一会也没啥问题，所以就不用邮件通知了。
 */
@RequiredArgsConstructor
@Getter
final class FriendAccident implements Activity {

	public enum Type {

		/** 搬家了 */
		MOVED,

		/** 失联了 */
		LOST,

		/** 分手了 */
		ABANDONED_ME,
	}

	private final Type type;

	private final String name;
	private final URI url;

	@Nullable
	private final URI newUrl;

	@Override
	public ActivityType getActivityType() {
		return ActivityType.FRIEND;
	}
}
