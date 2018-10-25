package net.kaciras.blog.api.discuss;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.kaciras.blog.infrastructure.TextUtils;
import net.kaciras.blog.infrastructure.exception.DataTooBigException;
import net.kaciras.blog.infrastructure.exception.LegallyProhibitedException;
import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import net.kaciras.blog.infrastructure.message.MessageClient;
import net.kaciras.blog.infrastructure.sql.DBUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.time.LocalDateTime;

@Data
@Configurable
public final class Discussion {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DiscussionDAO dao;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MessageClient messageClient;

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private long id;

	private int objectId;
	private int type;

	private int floor;
	private long parent;

	private int userId;
	private String content;

	private LocalDateTime time;
	private boolean deleted;

	private int voteCount;

	public ReplyList getReplyList() {
		if (parent != 0) {
			throw new ResourceNotFoundException("楼中楼不能再包含楼中楼了。<-- 有点绕口");
		}
		return new ReplyList(this);
	}

	public VoterList getVoterList() {
		return new VoterList(this.id);
	}

	// delete和restore这两个方法我认为应该放在Domain Object里
	// 因为它们是对deleted属性的修改，而不是真正的删除

	public void delete() {
		DBUtils.checkEffective(dao.updateDeleted(id, true));
	}

	public void restore() {
		DBUtils.checkEffective(dao.updateDeleted(id, false));
	}

	public static Discussion create(int userId, String content) {
		if (content == null || content.length() == 0) {
			throw new RequestArgumentException("评论内容不能为空");
		}
		if (TextUtils.getHeight(content, 40) > 64) {
			throw new DataTooBigException("评论内容过长，请分多次发表");
		}
		if (TextUtils.isDanger(content)) {
			throw new LegallyProhibitedException("评论包含不和谐内容");
		}
		var dis = new Discussion();
		dis.setUserId(userId);
		dis.setContent(content);
		return dis;
	}
}
