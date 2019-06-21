package net.kaciras.blog.api.ratelimit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;

public interface RateLimiterChecker {

	boolean check(InetAddress address, HttpServletRequest request, HttpServletResponse response) throws IOException;
}
