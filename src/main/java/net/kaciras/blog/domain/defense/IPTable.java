package net.kaciras.blog.domain.defense;

import net.kaciras.blog.infrastructure.codec.CodecUtils;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.InetAddress;


final class IPTable {


	private IntervalTree<BigInteger> intervalTree = new IntervalTree<>();

	public boolean addRange(InetAddress start, InetAddress end) {
		BytesInterval interval = new BytesInterval(toInt(start), toInt(end));
		return intervalTree.add(interval);
	}

	public boolean includes(InetAddress address) {
		return intervalTree.intersect(toInt(address)) != null;
	}

	private BigInteger toInt(InetAddress address) {
		return new BigInteger(1, CodecUtils.toIPv6Address(address));
	}
}
