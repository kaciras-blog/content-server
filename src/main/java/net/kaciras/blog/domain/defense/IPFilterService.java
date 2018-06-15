package net.kaciras.blog.domain.defense;

import net.kaciras.blog.infrastructure.exception.RequestArgumentException;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class IPFilterService {

	private final IPTable table = new IPTable();

	public boolean test(InetAddress address) {
		return true;
	}

	public void addRange(String start, String end) {
		try {
			InetAddress startAddr = InetAddress.getByName(start);
			InetAddress endAddr = InetAddress.getByName(end);

		} catch (UnknownHostException e) {
			throw new RequestArgumentException("输入的地址格式不正确");
		}
	}
}
