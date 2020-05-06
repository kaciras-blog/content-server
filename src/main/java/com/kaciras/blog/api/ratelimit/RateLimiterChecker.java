package com.kaciras.blog.api.ratelimit;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

public interface RateLimiterChecker {

	long check(InetAddress address, HttpServletRequest request);
}
