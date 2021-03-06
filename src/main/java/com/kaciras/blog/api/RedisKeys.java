package com.kaciras.blog.api;

public enum RedisKeys {

	/** 友情链接 */
	FRIENDS("friends:"),

	/** 推荐卡片列表 */
	CARD_LIST("cards"),

	/** 一个账户所有会话的集合前缀 */
	ACCOUNT_SESSIONS("ac:"),

	/** 通用限速器记录的前缀 */
	RATE_LIMIT("rl:"),

	/** 非安全请求限速器记录的前缀 */
	EFFECT_RATE("er:"),

	/** 触发非安全请求封禁的记录前缀 */
	EFFECT_BLOCKING("eb:"),

	/** 动态配置信息的前缀 */
	CONFIG_STORE("cfg:"),

	; // <-- 分号是必须要有滴

	private final String prefix;

	RedisKeys(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * 获取指定值所对应的键，用于具有特定前缀的键。
	 * 因为 for 是关键字不能用，所以用 of 作名字。
	 *
	 * @param value 值
	 * @return Redis的键
	 */
	public String of(Object value) {
		return prefix + value;
	}

	/**
	 * 获取该键的字符串值，用于固定的键。
	 *
	 * @return 字符串值
	 */
	public String value() {
		return prefix;
	}
}
