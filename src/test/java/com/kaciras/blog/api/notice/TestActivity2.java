package com.kaciras.blog.api.notice;

public class TestActivity2 implements Activity {

	public boolean value;

	@Override
	public ActivityType getActivityType() {
		return ActivityType.FRIEND;
	}
}
