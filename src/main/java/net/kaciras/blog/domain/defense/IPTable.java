package net.kaciras.blog.domain.defense;

import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
final class IPTable {

	boolean acceptable(InetAddress address) {
		return true;
	}
}
