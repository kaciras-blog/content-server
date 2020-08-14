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

	/**
	 * 该网站能否正常访问
	 */
	@Getter
	private final boolean alive;

	/**
	 * 该网站迁移到的新的地址，如果没有迁移则为null。
	 */
	@Nullable
	@Getter
	private final URI newUrl;

	private final String myOrigin;
	private final String html;

	/**
	 * 页面里是否存在本站的链接（互链）。
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
