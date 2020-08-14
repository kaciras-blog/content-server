package com.kaciras.blog.api.friend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * 处理友链验证逻辑的类，支持对网站的存活、迁移、互链做检查。
 */
@Component
final class FriendValidator {

	private final HttpClient httpClient;
	private final String myOrigin;

	private final String userAgent;

	public FriendValidator(HttpClient httpClient, @Value("${app.origin}") String myOrigin) {
		this.httpClient = httpClient;
		this.myOrigin = myOrigin;
		userAgent = String.format("KacirasBlog Friend Validator (+%s/about/blogger#friend", myOrigin);
	}

	/**
	 * 检查一个友链站点。
	 *
	 * @param uri 地址
	 * @return 检查结果
	 */
	public CompletableFuture<FriendSitePage> visit(URI uri) {
		var request = HttpRequest.newBuilder(uri)
				.header("User-Agent", userAgent)
				.timeout(Duration.ofSeconds(10));

		return httpClient
				.sendAsync(request.build(), HttpResponse.BodyHandlers.ofString())
				.thenApply(this::handleResponse)
				.exceptionally(e -> unavailable());
	}

	private FriendSitePage handleResponse(HttpResponse<String> response) {
		var hundred = response.statusCode() / 100;

		if (hundred != 2) {
			return unavailable();
		}

		URI newUrl = null;
		if (response.previousResponse().isPresent()) {
			newUrl = response.uri();
		}

		return new FriendSitePage(true, newUrl, myOrigin, response.body());
	}

	private FriendSitePage unavailable() {
		return new FriendSitePage(false, null, myOrigin, null);
	}
}
