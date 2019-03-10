package net.kaciras.blog.api;

public enum RedisKeys {

	SwiperList("swiper"),

	/** 一个账户所有会话的集合 */
	AccountSessions("ac:");

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
