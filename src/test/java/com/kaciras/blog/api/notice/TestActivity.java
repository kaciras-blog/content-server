package com.kaciras.blog.api.notice;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class TestActivity implements Activity {

	public int intValue;

	@Override
	public ActivityType getActivityType() {
		return ActivityType.DISCUSSION;
	}

	@Override
	public void sendMail(boolean clear, MailService sender) {
		if (clear) {
			sender.sendToAdmin("title", "content");
		}
	}
}
