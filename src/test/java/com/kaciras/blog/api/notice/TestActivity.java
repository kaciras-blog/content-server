package com.kaciras.blog.api.notice;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class TestActivity implements Activity, MailNotice {

	public int intValue;

	@Override
	public ActivityType getActivityType() {
		return ActivityType.Discussion;
	}

	@Override
	public void sendMail(boolean clear, MailService sender) {
		if(clear) {
			sender.sendToAdmin("title", "content");
		}
	}
}
