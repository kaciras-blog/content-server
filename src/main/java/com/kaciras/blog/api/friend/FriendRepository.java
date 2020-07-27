package com.kaciras.blog.api.friend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.RedisKeys;
import com.kaciras.blog.infra.exception.ResourceStateException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.time.Clock;
import java.util.List;

@SuppressWarnings("ConstantConditions")
@Repository
public class FriendRepository {

	private final Clock clock;

	private final BoundHashOperations<String, String, FriendLink> friendMap;
	private final BoundListOperations<String, String> hostList;
	private final BoundListOperations<String, String> hostListTemp;

	FriendRepository(Clock clock, RedisConnectionFactory redisFactory, ObjectMapper objectMapper) {
		this.clock = clock;

		var hashSerializer = new Jackson2JsonRedisSerializer<>(FriendLink.class);
		hashSerializer.setObjectMapper(objectMapper);

		var template = new RedisTemplate<String, String>();
		template.setConnectionFactory(redisFactory);
		template.setDefaultSerializer(RedisSerializer.string());
		template.setHashValueSerializer(hashSerializer);
		template.afterPropertiesSet();

		friendMap = template.boundHashOps(RedisKeys.Friends.of("map"));
		hostList = template.boundListOps(RedisKeys.Friends.of("list"));
		hostListTemp = template.boundListOps(RedisKeys.Friends.of("temp"));
	}

	public FriendLink[] getFriends() {
		return getFriends(hostList.range(0, -1));
	}

	public FriendLink[] getFriends(List<String> hosts) {
		var map = friendMap.entries();
		return hosts.stream().map(map::get).toArray(FriendLink[]::new);
	}

	public String addFriend(FriendLink friend) {
		var host = URI.create(friend.url).getHost();
		friend.createTime = clock.instant();

		if (friendMap.putIfAbsent(host, friend)) {
			hostList.rightPush(host);
		} else {
			throw new ResourceStateException("指定站点的友链已存在");
		}

		return host;
	}

	public boolean remove(String host) {
		hostList.remove(1, host);
		return friendMap.delete(host) != null;
	}

	public void updateSort(String[] newList) {
		hostListTemp.rightPushAll(newList);
		hostListTemp.rename(hostList.getKey());
	}
}
