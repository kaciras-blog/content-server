package com.kaciras.blog.api.account.local;

import com.kaciras.blog.api.account.HttpSessionTable;
import lombok.*;
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
	 * 使用 SHA3-512 处理密码，当然 SHA-512 也没有安全问题但我赶个时髦。
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
	private HttpSessionTable httpSessionTable;

	/**
	 * 为了省事，账号的 ID 等于用户的 ID。
	 * 因为用户可以用其他方式登录，所以这里的 ID 不是连续的。
	 */
	private int id;

	private String name;

	private byte[] password;
	private byte[] salt;

	/**
	 * 修改密码，成功后所有与该用户相关的会话将注销。
	 * 不要使用 setPassword() 来修改，那个方法只是给 Mybatis 用的。
	 *
	 * @param password 新密码
	 */
	public void changePassword(String password) {
		encryptPassword(password);
		accountDAO.updatePassword(this);
		httpSessionTable.clearAll(id);
	}

	/**
	 * 检查给定的密码能否登陆该账号。
	 *
	 * @param passText 密码
	 * @return 如果密码正确返回true，否则false
	 */
	public boolean checkLogin(String passText) {
		return Arrays.equals(password, encryptPassword(passText, salt));
	}

	/**
	 * 对密码使用 HASH 加密，使用生成随机的盐值。
	 *
	 * @param password 明文密码
	 */
	private void encryptPassword(String password) {
		this.salt = new byte[HASH_SIZE >> 3];
		SECURE_RANDOM.nextBytes(salt);
		this.password = encryptPassword(password, salt);
	}

	/**
	 * 使用指定的盐值与密码混合，并进行 HASH 加密。
	 *
	 * <h2>HAMC</h2>
	 * SHA3 不需要采用 HMAC 来加盐，直接跟密码连一起即可。
	 * <a href="https://crypto.stackexchange.com/a/17928">参考</a>
	 *
	 * @param password 明文密码
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
			throw new Error("Your java do not support SHA3");
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
