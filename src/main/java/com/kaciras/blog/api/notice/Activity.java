package com.kaciras.blog.api.notice;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Activity {

	@JsonIgnore
	ActivityType getActivityType();
}
