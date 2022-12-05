package com.kaciras.blog.api.account;

import com.kaciras.blog.api.RedisKeys;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Component;

/**
 * 为了保证服务端能够主动删除 Session，使用了 Redis 的 Set 来记录每个用户的 Session。
 * 该仓库使用 Redis 的 Set 来记录 UserId -> [SessionIds]。
 *
 * <h2>RedisTemplate 的返回值</h2>
 * SpringDataRedis 的脑残设计，只有事务下才会返回 null 但 RedisTemplate 无法区分也没有事务，所以这里直接忽略警告。
 */
@RequiredArgsConstructor
@SuppressWarnings("ConstantConditions")
@Component
public class HttpSessionTable {

	private final SessionRepository<?> sessionRepository;
	private final RedisTemplate<String, String> redisTemplate;

	/**
	 * 记录用户的 HTTP 会话 ID 以便后面能够删除会话。
	 *
	 * @param userId    用户ID
	 * @param sessionId 会话ID
	 */
	public void add(int userId, String sessionId) {
		redisTemplate.boundSetOps(RedisKeys.ACCOUNT_SESSIONS.of(userId)).add(sessionId);
	}

	/**
	 * 删除指定用户所有的会话，这将注销该用户的所有终端的登录。
	 *
	 * @param userId 用户ID
	 */
	public void clearAll(int userId) {
		var key = RedisKeys.ACCOUNT_SESSIONS.of(userId);
		var records = redisTemplate.opsForSet().members(key);

		redisTemplate.unlink(key);
		records.forEach(sessionRepository::deleteById);
	}

	/**
	 * 会话如果过期，那么也没有办法及时清理，所以需要搞一个定时清理。
	 */
	@Scheduled(fixedDelay = 24 * 60 * 60 * 1000)
	void cleanAccountRecords() {
		var options = ScanOptions.scanOptions()
				.match(RedisKeys.ACCOUNT_SESSIONS.of("*"))
				.build();

		@Cleanup var conn = redisTemplate.getRequiredConnectionFactory().getConnection();
		@Cleanup var accounts = conn.keyCommands().scan(options);

		while (accounts.hasNext()) {
			var recordSet = new String(accounts.next());
			var invalid = redisTemplate.opsForSet().members(recordSet)
					.stream()
					.filter(id -> sessionRepository.findById(id) == null)
					.toArray();
			if (invalid.length > 0) {
				redisTemplate.opsForSet().remove(recordSet, invalid);
			}
		}
	}
}
