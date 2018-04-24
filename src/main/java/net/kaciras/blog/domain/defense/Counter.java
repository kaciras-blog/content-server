package net.kaciras.blog.domain.defense;

import lombok.Getter;
import lombok.ToString;

@ToString
public final class Counter {

	@Getter
	private int value;

	private int mark;

	public void mark() {
		mark = value;
	}

	public int difference() {
		return value - mark;
	}

	public int incrementAndGet() {
		return ++value;
	}

}
