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

	/** 使用512位的Sha3算法对密码加密 */
	private static final int HASH_SIZE = 512;

	static LoginRecordDao loginRecordDao;
	static BanRecordDao banRecordDao;

	static Cache<Integer, LoginRecord> cache;

	User(int id, String name) {
		this.id = id;
		this.name = name;
	}

	private int id;
	private String name;
	private byte[] password;
	private byte[] salt;

	private String email;
	private ImageRefrence head = ImageRefrence.internal("noface.gif");

	private boolean deleted;

	private LocalDateTime regTime;
	private InetAddress regAddress;

	LocalDateTime getBannedEndTime() {
		return banRecordDao.selectLastEndTime(id);
	}

	void putPassword(String passText) {
		salt = new byte[HASH_SIZE >> 3];
		Utils.SECURE_RANDOM.nextBytes(salt);
		password = encryptPassword(passText, salt);
	}

	boolean checkLogin(String passText) {
		if(deleted) {
			throw new ResourceDeletedException("该用户已被删除");
		}
		LocalDateTime bannedEndTime = getBannedEndTime();
		if(bannedEndTime != null) {
			throw new ResourceStateException("用户已被封禁，解封时间:" + Utils.TIME_FORMATTER.format(bannedEndTime));
		}
		return Arrays.equals(password, encryptPassword(passText, salt));
	}

	/**
	 * 对密码进行HASH加密
	 *
	 * @param password 原始密码文本
	 * @param salt 盐值
	 * @return 加密后的密码
	 */
	private byte[] encryptPassword(String password, byte[] salt) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA3-" + HASH_SIZE);
			md.update(password.getBytes(StandardCharsets.UTF_8));
			md.update(salt);
			return md.digest();
		} catch (NoSuchAlgorithmException ex) {
			throw new Error("请使用支持SHA3的Java版本");
		}
	}

	List<LoginRecord> getLoginRecords() {
		return loginRecordDao.select(id);
	}

	List<BanRecord> getBanRecords() {
		return banRecordDao.selectBanRecords(id);
	}

	public void recordLogin(InetAddress address) {
		LoginRecord record = cache.get(id);
		if (record == null) {
			LoginRecord stored = loginRecordDao.selectMax(id);
			if (stored != null) {
				record = stored;
				cache.put(id, stored);
			}
		}
		if (record != null && record.getAddress().equals(address)) {
			return; //当前设计，仅地址相同就不记录
		}
		cache.remove(id);
		loginRecordDao.insert(id, address);
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
