package com.kaciras.blog.api.friend;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class FriendValidatorTest {

	private final HttpClient httpClient = HttpClient
			.newBuilder()
			.followRedirects(Redirect.ALWAYS)
			.build();

	private final FriendValidator validator = new FriendValidator(httpClient, "https://blog.example.com");

	private final List<HttpServer> servers = new ArrayList<>();

	@AfterEach
	void clearServers() {
		servers.forEach(server -> server.stop(0));
	}

	/**
	 * 创建一个测试用的HTTP服务器，该服务器在运行完一个用例之后将自动关闭。
	 * <p>
	 * 对请求处理器也做了封装，自动调用 HttpExchange.close() 省点事。
	 *
	 * @param handler 请求处理器
	 * @return 服务器的URI
	 */
	private URI createServer(HttpHandler handler) throws IOException {
		var server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/", e -> {
			handler.handle(e);
			e.close();
		});
		server.start();
		servers.add(server);
		return serverUri(server.getAddress());
	}

	private URI serverUri(InetSocketAddress address) {
		return URI.create("http://localhost:" + address.getPort());
	}

	@Test
	void dead() throws Exception {
		var result = validator.visit(URI.create("https://localhost:1")).get();
		assertThat(result.isAlive()).isFalse();
	}

	@Test
	void serverError() throws Exception {
		var uri = createServer(exchange -> exchange.sendResponseHeaders(500, 0));

		var result = validator.visit(uri).get();
		assertThat(result.isAlive()).isFalse();
	}

	@Test
	void userAgent() throws Exception {
		var uri = createServer(exchange -> {
			assertThat(exchange.getRequestHeaders().getFirst("User-Agent"))
					.isEqualTo("KacirasBlog Friend Validator (+https://blog.example.com/about/blogger#friend");

			exchange.sendResponseHeaders(200, 0);
		});

		var result = validator.visit(uri).get();
		assertThat(result.isAlive()).isTrue();
	}

	@Test
	void newUrl() throws Exception {
		var newSite = createServer(exchange -> exchange.sendResponseHeaders(200, 0));

		var redirect = createServer(exchange -> {
			exchange.getResponseHeaders().add("Location", newSite.toString());
			exchange.sendResponseHeaders(301, 0);
		});

		var result = validator.visit(redirect).get();
		assertThat(result.isAlive()).isTrue();
		assertThat(result.getNewUrl()).isEqualTo(newSite);
	}

	/**
	 * 想起来 Medium 网站就有无限重定向的问题，这里也测一下。
	 * <p>
	 * 从源码来看，jdk.internal.net.httpRedirectFilter#handleResponse 里有检查次数，应该不会出问题。
	 * jdk.httpclient.redirects.retrylimit 参数可以设置重定向上限
	 */
	@Test
	void infiniteRedirect() throws Exception {
		var redirect = createServer(exchange -> {
			var self = serverUri(exchange.getLocalAddress());
			exchange.getResponseHeaders().add("Location", self.toString());
			exchange.sendResponseHeaders(301, 0);
		});

		var result = validator.visit(redirect).get();
		assertThat(result.isAlive()).isFalse();
	}

	@Test
	void hasMyLink() throws Exception {
		var uri = createServer(exchange -> {
			var html = new ClassPathResource("friend-validate-2.html");
			exchange.sendResponseHeaders(200, html.getFile().length());
			html.getInputStream().transferTo(exchange.getResponseBody());
		});

		var result = validator.visit(uri).get();
		assertThat(result.isAlive()).isTrue();
		assertThat(result.hasMyLink()).isTrue();
	}

	@Test
	void notHasMyLink() throws Exception {
		var uri = createServer(exchange -> {
			var html = new ClassPathResource("friend-validate.html");
			exchange.sendResponseHeaders(200, html.getFile().length());
			html.getInputStream().transferTo(exchange.getResponseBody());
		});

		var result = validator.visit(uri).get();
		assertThat(result.isAlive()).isTrue();
		assertThat(result.hasMyLink()).isFalse();
	}
}
