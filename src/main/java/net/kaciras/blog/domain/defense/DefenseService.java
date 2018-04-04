package net.kaciras.blog.domain.defense;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.InetAddress;

@RequiredArgsConstructor
@Service
public class DefenseService {

	private final IPTable ipTable;
	private final FrequencyLimiter frequencyLimiter;
	private final ProxyDetector proxyDetector;

	public boolean accept(InetAddress address) {
		return ipTable.acceptable(address)
				&& frequencyLimiter.isAllow(address)
				&& !proxyDetector.isProxy(address);
	}
}
