package net.kaciras.blog.domain.defense;

import java.math.BigInteger;

public class BytesInterval implements Interval<BigInteger> {

	private final BigInteger lowerPoint;
	private final BigInteger higherPoint;

	public BytesInterval(BigInteger lowerPoint, BigInteger higherPoint) {
		this.lowerPoint = lowerPoint;
		this.higherPoint = higherPoint;
	}

	@Override
	public BigInteger low() {
		return lowerPoint;
	}

	@Override
	public BigInteger high() {
		return higherPoint;
	}
}
