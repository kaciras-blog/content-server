package net.kaciras.blog.domain.discuss;

import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.infrastructure.message.MessageClient;
import net.kaciras.blog.infrastructure.message.event.DiscussionDeletedEvent;
import net.kaciras.blog.infrastructure.message.event.DiscussionRestoreEvent;
import net.kaciras.blog.infrastructure.message.event.DiscussionVoteEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Configurable
public class Discussion implements Serializable {

	@Autowired
	static MessageClient messageClient;

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private int id;
	private int userId;

	private int postId;
	private int floor;

	private int parent;

	private String content;

	private int voteCount;

	private LocalDateTime time;
	private boolean deleted;

	/**
	 * 点赞，一个用户只能点赞一次
	 *
	 * @param userId 点赞用户的id
	 */
	void addVote(int userId) {
		voteCount++;
		DiscussionVoteEvent event = new DiscussionVoteEvent();
		event.setRevoke(false);
		event.setDiscussionId(id);
		event.setUserId(userId);
		messageClient.send(event).blockingGet();
	}

	/**
	 * 取消点赞，只有先点赞了才能取消
	 *
	 * @param userId 点赞用户的id
	 */
	void removeVote(int userId) {
		voteCount--;
		DiscussionVoteEvent event = new DiscussionVoteEvent();
		event.setRevoke(true);
		event.setDiscussionId(id);
		event.setUserId(userId);
		messageClient.send(event).blockingGet();
	}

	// delete和restore这两个方法我认为应该放在Domain Object里
	// 因为它们是对deleted属性的修改，而不是真正的删除

	void delete() {
		DiscussionDeletedEvent event = new DiscussionDeletedEvent();
		event.setDiscussionId(id);
		messageClient.send(event).blockingGet();
	}

	void restore() {
		DiscussionRestoreEvent event = new DiscussionRestoreEvent();
		event.setDiscussionId(id);
		messageClient.send(event).blockingGet();
	}
}
