package com.kaciras.blog.infra.ratelimit;

import org.springframework.lang.NonNull;

/**
 * 限流器，用于限制对某个资源的访问速率。
 * <p>
 * 每一次访问都需要获取一定数量的令牌，令牌用来衡量一次访问对资源的消耗程度，
 * 越大表示越耗资源，需要被限制在更低的速率。
 *
 * 应用层的限流器无法处理底层的情况，比如 TCP 层的 DDOS 攻击，应对这些请使用防火墙或 CDN。
 */
public interface RateLimiter {

	/**
	 * 获取指定数量的令牌，如果无法获取则返回一个时间，
	 * 表示请求方需要至少等待该时间之后才有可能获取成功；如果获取成功则返回0。
	 * <p>
	 * 如果其它地方也以同样的ID获取了资源，则这里的请求方等待的时间也能会延长。
	 * 如果返回了一个负的时间，表明该请求无论如何都无法通过。
	 *
	 * @param id      标识获取者的身份，一般是对方的IP之类的
	 * @param permits 要获取的令牌数量
	 * @return 需要等待的时间（秒），0表示成功，负值表示永远无法完成
	 */
	long acquire(@NonNull String id, int permits);
}
