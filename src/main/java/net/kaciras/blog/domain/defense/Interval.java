package net.kaciras.blog.domain.defense;

public interface Interval<T extends Comparable<T>> {

	T low();

	T high();
}
