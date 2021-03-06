package com.kaciras.blog.infra;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;
import reactor.netty.tcp.DefaultSslContextSpec;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class MiscTest {

	private static DisposableServer server;
	private static URI serverUri;

	@BeforeAll
	static void startServer() {
		((Logger) LoggerFactory.getLogger("io.netty")).setLevel(Level.OFF);
		((Logger) LoggerFactory.getLogger("reactor")).setLevel(Level.OFF);

		// SelfSignedCertificate 用了底层功能兼容性不太好，所以还是用预先生成好的证书。
		var loader = MiscTest.class.getClassLoader();
		var cert = loader.getResourceAsStream("localhost.pem");
		var key = loader.getResourceAsStream("localhost.pvk");
		var sslContextSpec = DefaultSslContextSpec.forServer(cert, key);

		server = HttpServer.create()
				.secure(spec -> spec.sslContext(sslContextSpec))
				.protocol(HttpProtocol.HTTP11, HttpProtocol.H2)
				.handle((inbound, outbound) -> outbound.sendString(Mono.just("Hello")))
				.bindNow();

		serverUri = URI.create("https://localhost:" + server.port());
	}

	@AfterAll
	static void closeServer() {
		server.disposeNow();
	}

	@Test
	void httpsURLConnectionFail() throws Exception {
		assertThatThrownBy(serverUri.toURL()::openStream).isInstanceOf(SSLHandshakeException.class);
	}

	@Test
	void httpsURLConnectionWithDisabled() throws Exception {
		var conn = ((HttpsURLConnection) serverUri.toURL().openConnection());

		var sslContext = Misc.createTrustAllSSLContext();
		conn.setHostnameVerifier((host, session) -> true);
		conn.setSSLSocketFactory(sslContext.getSocketFactory());

		try (var stream = conn.getInputStream()) {
			assertThat(stream.readAllBytes()).containsExactly("Hello".getBytes());
		}
	}

	@Test
	void httpClientFail() {
		var request = HttpRequest.newBuilder().uri(serverUri).build();
		assertThatThrownBy(() -> HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()))
				.isInstanceOf(IOException.class);
	}

	@Test
	void httpClientWithDisabling() throws Exception {
		var request = HttpRequest
				.newBuilder()
				.uri(serverUri).build();

		var response = HttpClient
				.newBuilder()
				.sslContext(Misc.createTrustAllSSLContext())
				.build()
				.send(request, HttpResponse.BodyHandlers.ofString());

		assertThat(response.body()).isEqualTo("Hello");
	}

	@Test
	void disableGlobal() throws Exception {
		var oldContext = SSLContext.getDefault();
		var oldSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
		var oldVerifier = HttpsURLConnection.getDefaultHostnameVerifier();

		Misc.disableHttpClientCertificateVerify();

		try (var input = serverUri.toURL().openStream()) {
			assertThat(new String(input.readAllBytes())).isEqualTo("Hello");
		}

		var request = HttpRequest.newBuilder().uri(serverUri).build();
		var r2 = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
		assertThat(r2.body()).isEqualTo("Hello");

		SSLContext.setDefault(oldContext);
		HttpsURLConnection.setDefaultSSLSocketFactory(oldSocketFactory);
		HttpsURLConnection.setDefaultHostnameVerifier(oldVerifier);
	}
}
