package com.kaciras.blog.api.friend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.RedisKeys;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.util.List;
import java.util.Map;

/**
 * 友链的存储服务，保存了友链的信息及排序。
 * <p>
 * 【一致性问题】
 * 这里要求同一时刻只能调用一个修改方法，因为只有博主一人能修改所以是可行的，不过要防止重复提交。
 * 这保证了修改操作之间无需考虑一致性问题。
 * <p>
 * 在对两个存储的访问上遵循以下原则：
 * 1）添加时先加 HASH 后加 LIST，删除时反之，保证 HASH 里一定包含 LIST 的元素。
 * 2）getFriends() 使用事务同时查询出 LIST 和 HASH。
 * 这两条确保了查询操作不会出现null元素，且博客系统的实时性要求不大，所以即使查到了旧内容也无关紧要。
 * <p>
 * 另外重排序使用临时列表 + RENAME 的方式保证了原子性，不会出现 LIST 为空的中间情况。
 */
@SuppressWarnings({"ConstantConditions", "NullableProblems"})
@Repository
public class FriendRepository {

	private final Clock clock;

	private final RedisTemplate<String, String> template;

	private final BoundHashOperations<String, String, FriendLink> friendMap;
	private final BoundListOperations<String, String> hostList;
	private final BoundListOperations<String, String> hostListTemp;

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
		hostListTemp = template.boundListOps(RedisKeys.Friends.of("temp"));
	}

	/**
	 * 获取友链列表，使用内部维护的顺序。
	 *
	 * @return 友链列表
	 */
	@SuppressWarnings("unchecked")
	public FriendLink[] getFriends() {
		List<?> snapshot = template.execute(new SessionCallback<>() {
			public <K, V> List<?> execute(RedisOperations<K, V> operations) {
				operations.multi();
				hostList.range(0, -1);
				friendMap.entries();
				return operations.exec();
			}
		});
		// JAVA的垃圾类型系统不支持单独标记List元素的类型，所以只能强制转换
		var list = (List<String>) snapshot.get(0);
		var map = (Map<String, FriendLink>) snapshot.get(1);

		return list.stream().map(map::get).toArray(FriendLink[]::new);
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
			return true;
		}
		return false;
	}

	/**
	 * 删除指定域名的友链。
	 *
	 * @param host 域名
	 * @return 如果友链存在且成功删除则为true，否则false
	 */
	public boolean remove(String host) {
		hostList.remove(1, host);
		return friendMap.delete(host) != 0;
	}

	/**
	 * 根据给定的域名列表更新友链的排序。
	 * <p>
	 * 域名列表里必须包含与原来同样的元素，否则会出现一致性问题。
	 *
	 * @param newList 新的域名列表
	 */
	public void updateSort(String[] newList) {
		hostListTemp.rightPushAll(newList);
		hostListTemp.rename(hostList.getKey());
	}
}
