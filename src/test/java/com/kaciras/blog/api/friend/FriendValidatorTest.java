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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class FriendValidatorTest {

	private final HttpClient httpClient = HttpClient
			.newBuilder()
			.followRedirects(HttpClient.Redirect.ALWAYS)
			.build();

	private final FriendValidator validator = new FriendValidator(httpClient, "https://blog.example.com");

	private final List<HttpServer> servers = new ArrayList<>();

	private URI createServer(HttpHandler handler) throws IOException {
		var server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/", e -> {
			handler.handle(e);
			e.close();
		});
		server.start();
		servers.add(server);
		return URI.create("http://localhost:" + server.getAddress().getPort());
	}

	@AfterEach
	void clearServers() {
		servers.forEach(server -> server.stop(0));
	}

	@Test
	void dead() throws Exception {
		var uri = createServer(exchange -> exchange.sendResponseHeaders(500, 0));

		var result = validator.visit(uri).get();
		assertThat(result.isAlive()).isFalse();
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
