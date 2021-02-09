package com.kaciras.blog.infra;

import sun.misc.Unsafe;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.GeneralSecurityException;

/**
 * 乱七八糟的只与 JAVA 自身相关的工具方法。
 *
 * <h2>不使用 Lombok 的 @UtilityClass</h2>
 * 因为自动添加的 static 和 final 会混淆 Java 的语法降低可读性，
 * 同时自动生成的构造方法内部抛异常代码是多余的，我肯定不会调用，还干扰覆盖率统计。
 */
public final class Misc {

	private Misc() {}

	/**
	 * 创建一个 SSLContext 对象，其接受所有证书不做任何检查。
	 *
	 * @return SSLContext对象
	 * @throws GeneralSecurityException 如果发生了错误
	 */
	public static SSLContext createTrustAllSSLContext() throws GeneralSecurityException {
		var sslc = SSLContext.getInstance("TLS");
		sslc.init(null, new TrustManager[]{new X509TrustAllManager()}, null);
		return sslc;
	}

	/**
	 * 屏蔽 HttpsURLConnection 和 HttpClient(Java11) 默认的证书检查。
	 * 该方法直接修改全局设置，可能会产生副作用，使用须谨慎。
	 *
	 * @throws GeneralSecurityException 如果发生了错误
	 */
	public static void disableHttpClientCertificateVerify() throws GeneralSecurityException {
		var sslc = createTrustAllSSLContext();
		SSLContext.setDefault(sslc);
		HttpsURLConnection.setDefaultSSLSocketFactory(sslc.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
	}

	/**
	 * 从Java9开始的模块系统禁止了一些不合法的访问，而很多第三方库仍然依赖这些操作，不合法的
	 * 访问在程序控制台中将输出几段警告信息，看着就烦，这里给禁止掉。
	 * <p>
	 * 具体做法是把 IllegalAccessLogger.logger 提前设置成 null，因为它是 OneShot 机制，只使用
	 * 一次之后就被设置为 null 避免重复打印。
	 * <p>
	 * 该修改过程本身就属于非法访问，为了不触发警告，必须用 Unsafe 里的方法而不能用反射。
	 */
	public static void disableIllegalAccessWarning() {
		try {
			var theUnsafe = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			var u = (Unsafe) theUnsafe.get(null);

			var cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
			var logger = cls.getDeclaredField("logger");
			u.putObjectVolatile(cls, u.staticFieldOffset(logger), null);
		} catch (ClassNotFoundException ignore) {
		} catch (Exception e) {
			throw new Error("An error occurred when disable illegal access warning", e);
		}
	}
}
