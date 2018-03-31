package net.kaciras.misc;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;

final class MiscTest {

	/**
	 * 验证JAVA默认Sha3摘要的实现，对消息片段的多次update后的摘要输出，
	 * 与一次性对整个消息的摘要输出结果相同。
	 */
	@Test
	void testSha3ConcatMessage() throws Exception {
		String a = "hellow";
		String b = "world";

		MessageDigest md = MessageDigest.getInstance("SHA3-256");
		byte[] digestA = md.digest((a + b).getBytes());

		md = MessageDigest.getInstance("SHA3-256");
		md.update(a.getBytes());
		byte[] digestB = md.digest(b.getBytes());

		md = MessageDigest.getInstance("SHA3-256");
		md.update(a.getBytes());
		md.update(b.getBytes());
		byte[] digestC = md.digest();

		Assertions.assertThat(digestA).isEqualTo(digestB);
		Assertions.assertThat(digestB).isEqualTo(digestC);
	}
}
