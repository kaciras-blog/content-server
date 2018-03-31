package net.kaciras.blog.domain;

public enum CheckType {

	@EnumConfigItem("无审核")
	NO_FILTE,

	@EnumConfigItem("需要审核")
	AUDIT,

	@EnumConfigItem("禁止评论")
	DISABLE,
}
