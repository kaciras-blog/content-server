package com.kaciras.blog.infra;

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
}
