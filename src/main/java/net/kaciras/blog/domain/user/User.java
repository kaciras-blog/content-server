package net.kaciras.blog.domain.user;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.kaciras.blog.domain.Utils;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;
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

	private static final int SALT_SIZE = 32;

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

	private LocalDateTime regTime;
	private InetAddress regAddress;

	void putPassword(String passText) {
		salt = new byte[SALT_SIZE];
		Utils.SECURE_RANDOM.nextBytes(salt);
		password = encryptPassword(passText, salt);
	}

	boolean checkLogin(String passText) {
		return Arrays.equals(password, encryptPassword(passText, salt));
	}

	private byte[] encryptPassword(String password, byte[] salt) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA3-256");
			md.update(password.getBytes(StandardCharsets.UTF_8));
			md.update(salt);
			return md.digest();
		} catch (NoSuchAlgorithmException ex) {
			throw new Error("请使用支持SHA3的Java版本");
		}
	}

	public List<LoginRecord> getLoginRecords() {
		return loginRecordDao.select(id);
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
	 */
	void ban(int operator, Duration time, String cause) {
		LocalDateTime start = LocalDateTime.now();
		BanRecord record = new BanRecord()
				.start(start)
				.end(start.plus(time))
				.operator(operator)
				.cause(cause);
		banRecordDao.insertBanRecord(id, record);
	}

	void unBan(int bid, int operator, String cause) {
		banRecordDao.insertUnbanRecord(bid, operator, cause);
	}
}
