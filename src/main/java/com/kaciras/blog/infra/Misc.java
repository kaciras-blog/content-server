package com.kaciras.blog.infra;

import sun.misc.Unsafe;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.servlet.http.HttpServletRequest;
import java.security.GeneralSecurityException;
import java.util.NoSuchElementException;

/*
 * 我不喜欢用 lombok 的 @UtilityClass，它生成的构造方法内部抛异常纯属多余，正常人都不会去创建工具类
 * 的实例，同时它自动添加 static 和 final 会混淆Java的语法。
 */
public final class Misc {

	private Misc() {}

	/**
	 * 创建一个SSLContext对象，其被初始化为接受所有证书。
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
	 * 【警告】该方法直接修改全局设置，可能会产生副作用，使用须谨慎。
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

	/**
	 * Helper method to get first element from a iterable.
	 *
	 * @param iterable iterable object.
	 * @param <T>      type of the element.
	 * @return the element.
	 * @throws NoSuchElementException if iterable has no element.
	 */
	public static <T> T getFirst(Iterable<T> iterable) {
		var iter = iterable.iterator();
		if (iter.hasNext()) {
			return iter.next();
		}
		throw new NoSuchElementException("iterable has no element.");
	}

	/**
	 * 判断一个请求对象是否是不改变状态的安全请求。安全请求的定义见：
	 * https://tools.ietf.org/html/rfc7231#section-4.2.1
	 *
	 * 【注意】
	 * 这里去掉了 TRACE 方法，因为我用不到它，而且它的功能还有些安全隐患。
	 *
	 * @param request 请求对象
	 * @return 如果是安全请求则为true，否则false
	 */
	public static boolean isSafeRequest(HttpServletRequest request) {
		switch (request.getMethod()) {
			case "HEAD":
			case "GET":
			case "OPTIONS":
				return true;
			default:
				return false;
		}
	}
}
