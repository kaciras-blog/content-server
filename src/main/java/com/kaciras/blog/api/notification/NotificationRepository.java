package com.kaciras.blog.api.notification;

import com.kaciras.blog.api.discuss.Discussion;
import com.kaciras.blog.api.friend.FriendLink;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRepository {

	public Object getAll() {
		throw new UnsupportedOperationException("TODO");
	}

	public void clear() {

	}

	public void reportFriend(FriendLink friend, FriendAccident.Type type) {

	}

	public void reportNewDiscussion(Discussion discussion) {

	}
}
