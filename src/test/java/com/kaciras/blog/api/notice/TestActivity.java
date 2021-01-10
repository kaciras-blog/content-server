package com.kaciras.blog.api.notice;

public class TestActivity implements HttpNotice {

	@Override
	public String getKind() {
		return "test";
	}
}
