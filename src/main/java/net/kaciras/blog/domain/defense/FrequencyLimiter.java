package net.kaciras.blog.domain.defense;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
final class FrequencyLimiter {

	private final ThreadPoolTaskScheduler taskScheduler;
	private final ConcurrentHashMap<InetAddress, Integer> records = new ConcurrentHashMap<>();

	@Setter
	private int threshold = 10;

	@Setter
	@Getter
	private boolean enable;

	boolean isAllow(InetAddress address) {
		if (!enable) {
			return true;
		}
		Integer count = records.get(address);
		if (count == null) {
			count = 0;
		} else if (count > threshold) {
			return false;
		}
		records.put(address, ++count);
		taskScheduler.getScheduledExecutor().schedule(() -> records.remove(address), 30, TimeUnit.MINUTES);
		return true;
	}
}
