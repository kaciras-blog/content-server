package net.kaciras.blog.api.user;

import lombok.*;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Data
@Configurable
public class User {

	public static final User GUEST = new User(0, "游客");

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private BanRecordDAO banRecordDAO;

	private int id;
	private String name;

	private String email;
	private ImageRefrence head;

	private boolean deleted;

	private User(int id, String name) {
		this.id = id;
		this.name = name;
	}

	@Nullable
	public LocalDateTime getBannedEndTime() {
		return banRecordDAO.selectLastEndTime(id);
	}

	public List<BanRecord> getBanRecords() {
		return banRecordDAO.selectBanRecords(id);
	}

	/**
	 * 封禁该用户
	 *
	 * @param operator 操作者id
	 * @param time     封禁时间
	 * @param cause    封禁原因描述
	 * @return 封禁记录的id
	 */
	int ban(int operator, Duration time, String cause) {
		var start = LocalDateTime.now();
		var record = new BanRecord()
				.setStart(start)
				.setEnd(start.plus(time))
				.setOperator(operator)
				.setCause(cause);
		banRecordDAO.insertBanRecord(id, record);
		return record.getId();
	}

	void unBan(int bid, int operator, String cause) {
		banRecordDAO.insertUnbanRecord(bid, operator, cause);
	}
}
