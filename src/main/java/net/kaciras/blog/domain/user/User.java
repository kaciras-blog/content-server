package net.kaciras.blog.domain.user;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.kaciras.blog.domain.Utils;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;
import net.kaciras.blog.infrastructure.exception.ResourceDeletedException;
import net.kaciras.blog.infrastructure.exception.ResourceStateException;
import org.ehcache.Cache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Data
public class User {

	static BanRecordDao banRecordDao;

	private int id;
	private String name;

	private String email;
	private ImageRefrence head = ImageRefrence.parse("noface.gif");

	private boolean deleted;

	User(int id, String name) {
		this.id = id;
		this.name = name;
	}

	@Nullable
	LocalDateTime getBannedEndTime() {
		return banRecordDao.selectLastEndTime(id);
	}

	List<BanRecord> getBanRecords() {
		return banRecordDao.selectBanRecords(id);
	}

	/**
	 * 封禁该用户
	 *
	 * @param operator 操作者id
	 * @param time 封禁时间
	 * @param cause 封禁原因描述
	 * @return 封禁记录的id
	 */
	int ban(int operator, Duration time, String cause) {
		LocalDateTime start = LocalDateTime.now();
		BanRecord record = new BanRecord()
				.setStart(start)
				.setEnd(start.plus(time))
				.setOperator(operator)
				.setCause(cause);
		banRecordDao.insertBanRecord(id, record);
		return record.getId();
	}

	void unBan(int bid, int operator, String cause) {
		banRecordDao.insertUnbanRecord(bid, operator, cause);
	}
}
