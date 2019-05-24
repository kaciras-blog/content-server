package net.kaciras.blog.api.principal.local;

import lombok.*;
import net.kaciras.blog.api.principal.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name"})
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Data
@Configurable
public final class Account {

	/**
	 * 使用512位的Sha3算法对密码加密
	 */
	private static final int HASH_SIZE = 512;
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private AccountDAO accountDAO;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private SessionRepository sessionRepository;


// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

	private int id;
	private String name;

	private byte[] password;
	private byte[] salt;

	/**
	 * 修改密码，修改成功后所有与该用户相关的会话将注销。
	 *
	 * @param password 新密码
	 */
	public void changePassword(String password) {
		encryptPassword(password);
		accountDAO.updatePassword(this);
		sessionRepository.clearAll(id);
	}

	/**
	 * 检查给定的密码能否登陆该账号。
	 *
	 * @param passText 密码
	 * @return 如果密码正确返回true
	 */
	public boolean checkLogin(String passText) {
		return Arrays.equals(password, encryptPassword(passText, salt));
	}

	private void encryptPassword(String password) {
		this.salt = new byte[HASH_SIZE >> 3];
		SECURE_RANDOM.nextBytes(salt);
		this.password = encryptPassword(password, salt);
	}

	/**
	 * 生成盐值，并对密码进行HASH加密。
	 * SHA3算法不需要采用HMAC来加盐，直接跟密码连在一起即可。
	 *
	 * @param password 原始密码文本
	 * @param salt     盐值
	 * @return 加密后的密码
	 */
	private static byte[] encryptPassword(String password, byte[] salt) {
		try {
			var sha3 = MessageDigest.getInstance("SHA3-" + HASH_SIZE);
			sha3.update(password.getBytes(StandardCharsets.UTF_8));
			sha3.update(salt);
			return sha3.digest();
		} catch (NoSuchAlgorithmException ex) {
			throw new Error("Your java version do not support SHA3");
		}
	}

	/**
	 * 创建一个新账户对象，其密码将使用随机的盐值加密。
	 *
	 * @param name     账户名称
	 * @param password 密码
	 * @return 账户对象
	 */
	public static Account create(int id, String name, String password) {
		var account = new Account();
		account.setId(id);
		account.setName(name);
		account.encryptPassword(password);
		return account;
	}
}
