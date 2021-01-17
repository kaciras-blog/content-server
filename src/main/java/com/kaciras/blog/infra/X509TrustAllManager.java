package com.kaciras.blog.infra;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.security.cert.X509Certificate;

/**
 * 信任所有证书的过管理器，用于测试或内部调用的情况。
 *
 * 这个类被排除在测试覆盖率之外，因为实在没啥好测的，调用方测测就够了。
 */
final class X509TrustAllManager extends X509ExtendedTrustManager {

	public void checkClientTrusted(X509Certificate[] certificates, String s, Socket socket) {}

	public void checkServerTrusted(X509Certificate[] certificates, String s, Socket socket) {}

	public void checkClientTrusted(X509Certificate[] certificates, String s, SSLEngine sslEngine) {}

	public void checkServerTrusted(X509Certificate[] certificates, String s, SSLEngine sslEngine) {}

	public void checkClientTrusted(X509Certificate[] chain, String authType) {}

	public void checkServerTrusted(X509Certificate[] chain, String authType) {}

	public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
}
