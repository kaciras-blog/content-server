package com.kaciras.blog.api.friend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.RedisKeys;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.time.Clock;

/**
 * 友链的存储服务，保存了友链的信息及排序，并提供 CURD 和重排功能。
 * <p>
 * 数据的持久化使用 Redis，用一个 HASH 保存友链对象，以及一个 LIST 保存顺序。
 * <p>
 * 【一致性问题】
 * 因为 SpringDataRedis 的事务写起来真的丑，所以这里使用了缓存模式避免事务。
 * getFriends() 始终返回缓存，启动和修改后都会更新缓存；
 * 单个查询方法 getFriend() 是原子的不存在一致性问题就不用管了。
 * <p>
 * 不过这也要求同一时刻只能调用一个修改方法，因为只有博主能修改所以是可以的。
 * 在 Controller 里用了 synchronized 防止并发，这使修改操作不会因为线程安全问题导致缓存更新错误。
 */
@SuppressWarnings("ConstantConditions")
@Repository
public class FriendRepository {

	private final Clock clock;

	private final RedisTemplate<String, String> template;

	private final BoundHashOperations<String, String, FriendLink> friendMap;
	private final BoundListOperations<String, String> hostList;

	private FriendLink[] cache;

	FriendRepository(Clock clock, RedisConnectionFactory redisFactory, ObjectMapper objectMapper) {
		this.clock = clock;

		var hashSerializer = new Jackson2JsonRedisSerializer<>(FriendLink.class);
		hashSerializer.setObjectMapper(objectMapper);

		template = new RedisTemplate<>();
		template.setConnectionFactory(redisFactory);
		template.setDefaultSerializer(RedisSerializer.string());
		template.setHashValueSerializer(hashSerializer);
		template.afterPropertiesSet();

		friendMap = template.boundHashOps(RedisKeys.Friends.of("map"));
		hostList = template.boundListOps(RedisKeys.Friends.of("list"));
	}

	/**
	 * 生成缓存，只有调用了此方法，用户看到的数据才会更新。
	 * <p>
	 * 在启动时调用确保缓存存在，修改后也要调用来刷新缓存。
	 */
	@PostConstruct
	private void generateCache() {
		var list = hostList.range(0, -1);
		var map = friendMap.entries();
		cache = list.stream().map(map::get).toArray(FriendLink[]::new);
	}

	/**
	 * 获取友链列表，使用内部维护的顺序。
	 *
	 * @return 友链列表
	 */
	public FriendLink[] getFriends() {
		if (cache != null) {
			return cache;
		}
		throw new IllegalStateException("竟然未生成缓存？");
	}

	/**
	 * 获取指定域名的友链。
	 *
	 * @param host 域名
	 * @return 友链对象，如果不存在则为null
	 */
	@Nullable
	public FriendLink get(String host) {
		return friendMap.get(host);
	}

	/**
	 * 添加一个友链，新的友链将处于末位。
	 *
	 * @param friend 友链
	 * @return 如果重复添加则为false，成功为true
	 */
	public boolean addFriend(FriendLink friend) {
		var host = friend.url.getHost();
		friend.createTime = clock.instant();

		if (friendMap.putIfAbsent(host, friend)) {
			hostList.rightPush(host);
			generateCache();
			return true;
		}
		return false;
	}

	/**
	 * 更新指定域名的友链，原域名的友链必须存在，否则不做任何改动。
	 *
	 * @param host   原域名
	 * @param friend 新友链
	 * @return false表示指定的原域名不存在，成功更新则为true
	 */
	public boolean updateFriend(String host, FriendLink friend) {
		var old = friendMap.get(host);
		if (old == null) {
			return false;
		}

		var newHost = friend.url.getHost();
		friend.createTime = old.createTime;

		friendMap.put(newHost, friend);

		if (!host.equals(newHost)) {
			var oldList = hostList.range(0, -1);
			hostList.set(oldList.indexOf(host), newHost);
			friendMap.delete(host);
		}

		generateCache();
		return true;
	}

	/**
	 * 删除指定域名的友链。
	 *
	 * @param host 域名
	 * @return 如果友链存在且成功删除则为true，否则false
	 */
	public boolean remove(String host) {
		if (hostList.remove(1, host) == 0) {
			friendMap.delete(host);
			generateCache();
			return true;
		}
		return false;
	}

	/**
	 * 根据给定的域名列表更新友链的排序。
	 * <p>
	 * 域名列表里必须包含与原来同样的元素，否则会出现一致性问题。
	 *
	 * @param newList 新的域名列表
	 */
	public void updateSort(String[] newList) {
		template.unlink(hostList.getKey());
		hostList.rightPushAll(newList);
		generateCache();
	}
}
