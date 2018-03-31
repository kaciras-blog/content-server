package net.kaciras.blog.facade;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class AccessFrequencyService {

	private final ThreadPoolTaskScheduler taskScheduler;
	private final ConcurrentHashMap<InetAddress, Integer> records = new ConcurrentHashMap<>();

	@Setter
	private int threshold = 10;

	@Setter
	@Getter
	private boolean enable;

	@Autowired
	public AccessFrequencyService(ThreadPoolTaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	public boolean isAllow(HttpServletRequest request) throws UnknownHostException {
		if (!enable) {
			return true;
		}
		InetAddress address = InetAddress.getByName(request.getRemoteAddr());
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
