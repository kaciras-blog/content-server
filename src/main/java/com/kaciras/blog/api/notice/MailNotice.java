package com.kaciras.blog.api.notice;

public interface MailNotice {

	void sendMail(boolean clear, MailService sender);
}
