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
 * 友链的存储服务，保存了友链的信息及排序，并提供CURD和重排功能。
 * <p>
 * 数据的持久化使用Redis，用一个 HASH 保存友链对象，以及一个 LIST 保存顺序。
 * <p>
 * 【一致性问题】
 * SpringDataRedis的事务写起来真的丑，所以这里尽量在不使用事务的情况下保证数据一致。
 * <p>
 * 首先要求同一时刻只能调用一个修改方法，因为只有博主一人能修改所以是可行的，不过要防止未看到结果前再次提交。
 * 这使修改操作之间无需考虑线程安全问题。
 * <p>
 * 在对两个存储的访问上遵循以下原则：
 * 1）添加时先加 HASH 后加 LIST，删除时反之，保证 HASH 里一定包含 LIST 的元素。
 * 2）getFriends() 使用事务同时查询出 LIST 和 HASH，这个事务是无法避免的。
 * 3）博客系统的不要求实时性，即使查到了旧内容也无关紧要。
 * 这些确保了 getFriends() 方法即使处于修改方法的中间状态，其结果也是正确的。
 * <p>
 * 重排序使用临时列表 + RENAME 的方式（类似CopyOnWrite）保证了原子性，不会出现 LIST 为空的中间情况。
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

		/*
		 * 两个存储必须用同一个 RedisTemplate，因为事务的序列化设置保存在里头。
		 * 恰好这里一个 HASH 一个 LIST 可以分别设置序列化方式，如果是有冲突的的话只能用 RedisCallback 的重载
		 * 来执行事务，然后自个处理序列化。
		 */
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

		return true;
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
	 * 域名列表里必须包含与原来同样的元素，否则会出现一致性问题。
	 * <p>
	 * 【坑】
	 * BoundKeyOperations.rename() 竟然不是简单地 RENAME，还把绑定的键给改了艹
	 *
	 * @param newList 新的域名列表
	 */
	public void updateSort(String[] newList) {
		hostListTemp.rightPushAll(newList);
		template.rename(hostListTemp.getKey(), hostList.getKey());
	}
}
