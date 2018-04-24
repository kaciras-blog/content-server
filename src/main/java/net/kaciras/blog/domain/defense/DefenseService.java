package net.kaciras.blog.domain.defense;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * HTTP层面的防御机制，包括防刷、IP黑名单、代理检测等。
 * 其中的部分功能也可以由底层实现。
 */
@RequiredArgsConstructor
@Service
public class DefenseService {

	private final IPTable ipTable;
	private final FrequencyLimiter frequencyLimiter;
	private final ProxyDetector proxyDetector;

	private List<Api> sensitiveApis = List.of(
			new Api(HttpMethod.GET, "/utils/captcha"),
			new Api(HttpMethod.POST, "/session/user"),
			new Api(HttpMethod.POST, "/users"),
			new Api(HttpMethod.POST, "/discussions")
	);

	public boolean accept(HttpServletRequest request) throws UnknownHostException {
		InetAddress address = InetAddress.getByName(request.getRemoteAddr());
		if(!ipTable.acceptable(address) && !proxyDetector.isProxy(address)) {
			return false;
		}
		for (Api api : sensitiveApis) {
			if(api.match(request)) {
				return frequencyLimiter.isAllow(address, api.pattern);
			}
		}
		return true;
	}

	private static final class Api {

		private HttpMethod method;
		private String pattern;

		private Api(HttpMethod method, String pattern) {
			this.method = method;
			this.pattern = pattern;
		}

		private boolean match(HttpServletRequest request) {
			return method.matches(request.getMethod()) && pattern.equals(request
					.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE));
		}
	}
}
