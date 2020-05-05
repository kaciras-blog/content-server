package net.kaciras.blog.api.ratelimit;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;

public interface RateLimiterChecker {

	long check(InetAddress address, HttpServletRequest request) throws IOException;
}
