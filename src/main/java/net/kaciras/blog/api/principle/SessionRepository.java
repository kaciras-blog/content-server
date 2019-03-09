package net.kaciras.blog.api.principle;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

/**
 * 为了保证服务端能够主动删除Session，使用了Redis的Set来记录每个用户的Session。
 * 该仓库使用Radis的Set来记录 UserId -> [SessionIds]。
 *
 * "SessionRepository" 这个名字和 Spring 内置的 Bean 冲突了，所以要改个名。
 */
@RequiredArgsConstructor
@Repository("AppSessionRepository")
class SessionRepository {

	/** Redis键前缀，例如用户ID=123的键位 ac:123 */
	private static final String PREFIX = "ac:";

	private final RedisTemplate<byte[], byte[]> redisTemplate;

	public void add(int userId, String sessionId) {
		redisTemplate.boundSetOps((PREFIX + userId).getBytes()).add(sessionId.getBytes());
	}

	/**
	 * 删除指定用户所有的会话，这将注销该用户的所有终端的登录。
	 * 
	 * @param userId 用户ID
	 */
	@SuppressWarnings("ConstantConditions")
	public void clearAll(int userId) {
		var key = (PREFIX + userId).getBytes();
		var records = redisTemplate.opsForSet().members(key);

		if (records != null) {
			records.stream()
					.filter(k -> Optional.ofNullable(redisTemplate.hasKey(k)).orElse(false))
					.forEach(redisTemplate::unlink);
			redisTemplate.unlink(key);
		}
	}

	/**
	 * 会话如果过期，那么也没有办法及时清理，所以需要搞一个定时清理。
	 */
	@Scheduled(fixedDelay = 24 * 60 * 60 * 1000)
	void cleanAccountRecords() {
		var options = ScanOptions.scanOptions().match(PREFIX + "*").build();
		var conn = redisTemplate.getRequiredConnectionFactory().getConnection();
		var accounts = conn.scan(options);

		while (accounts.hasNext()) {
			var recordSet = accounts.next();
			var invaild = Optional.ofNullable(redisTemplate.opsForSet().members(recordSet))
					.orElse(Set.of())
					.stream()
					.filter(k -> Optional.ofNullable(redisTemplate.hasKey(k)).orElse(false))
					.toArray();
			if(invaild.length > 0) {
				redisTemplate.opsForSet().remove(recordSet, invaild);
			}
		}
	}
}