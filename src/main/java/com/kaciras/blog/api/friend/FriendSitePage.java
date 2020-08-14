package com.kaciras.blog.api.friend;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.lang.Nullable;

import java.net.URI;
import java.util.function.Predicate;

@RequiredArgsConstructor
public final class FriendSitePage {

	@Getter
	private final boolean alive;

	@Nullable
	@Getter
	private final URI newUrl;

	private final String myOrigin;
	private final String html;

	/**
	 * 检查指定的HTML页面里是否存在本站的链接。
	 *
	 * @return 如果存在返回true
	 */
	public boolean hasMyLink() {
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
}
