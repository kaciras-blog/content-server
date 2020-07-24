package com.kaciras.blog.api.friend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaciras.blog.api.RedisKeys;
import com.kaciras.blog.api.notification.FriendAccident;
import com.kaciras.blog.api.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Clock;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Predicate;

@RequiredArgsConstructor
@Service
final class FriendValidateService {

	private final NotificationService notificationService;

	private final ObjectMapper objectMapper;
	private final Clock clock;
	private final HttpClient httpClient;
	private final RedisTemplate<String, byte[]> redis;

	private final String myOrigin = "https://blog.kaciras.com";

	/**
	 * 定时扫描对方的网站，检查是否嗝屁（默哀），以及单方面删除本站（为什么不跟人家做朋友了）。
	 * <p>
	 * 【安全性】
	 * 发送请求可能暴露服务器的地址，这种情况下可以通过 app.http-client.proxy 设置代理。
	 */
	@Scheduled(fixedDelay = 24 * 60 * 60 * 1000)
	void queueValidateTask() throws IOException {
		var queue = new LinkedList<ValidateRecord>();
		var map = redis.<String, byte[]>opsForHash().entries(RedisKeys.Friends.of("vR"));

		for (var e : map.entrySet()) {
			var record = objectMapper.readValue(e.getValue(), ValidateRecord.class);

			var p = record.failed > 0 ? Duration.ofDays(7) : Duration.ofDays(30);

			var checkDate = record.validate.plus(p);
			if (checkDate.isAfter(clock.instant())) queue.add(record);

		}

		validateFriends(queue);
	}

	private void validateFriends(Queue<ValidateRecord> queue) {
		if (queue.isEmpty()) {
			return;
		}
		var record = queue.remove();
		var userAgent = String.format("KacirasBlog Friend Validator (+%s/about/blogger#bot", myOrigin);
		var checkUrl = record.friendPage != null ? record.friendPage : record.url;

		var request = HttpRequest
				.newBuilder(URI.create(checkUrl))
				.header("User-Agent", userAgent)
				.timeout(Duration.ofSeconds(10));

		httpClient
				.sendAsync(request.build(), BodyHandlers.ofString())
				.thenAccept(res -> this.handleAliveResponse(record, res))
				.thenRunAsync(() -> validateFriends(queue));
	}

	private void handleAliveResponse(ValidateRecord record, HttpResponse<String> response) {
		record.validate = clock.instant();

		if (response.statusCode() / 100 != 2) {
			record.failed++;

			if (record.failed > 3) {
				record.failed = 0;
				notificationService.reportFriend(record.url, FriendAccident.Type.Inaccessible);
			}
			updateRecord(record);

		} else if (record.friendPage != null) {
			if (!checkMyLink(response.body())) {
				notificationService.reportFriend(record.url, FriendAccident.Type.AbandonedMe);
			}
			record.failed = 0;
			updateRecord(record);
		}
	}

	private boolean checkMyLink(String html) {

		Predicate<Element> isLinkToMySite = el -> {
			var href = URI.create(el.attr("href"));
			var origin = href.getScheme() + "://" + href.getHost();
			return myOrigin.equals(origin);
		};

		return Jsoup.parse(html)
				.getElementsByTag("a")
				.stream()
				.anyMatch(isLinkToMySite);
	}

	private void updateRecord(ValidateRecord record) {
		var host = URI.create(record.url).getHost();
		var exists = redis.opsForHash().hasKey(RedisKeys.Friends.of("vR"), host);
		if (exists) {
			redis.opsForHash().put(RedisKeys.Friends.of("vR"), host, record);
		}
	}
}
