package net.easecation.eccommons.adt;

import java.util.Objects;

/**
 * ADT type with A * B element
 */
public final class Tuple<A, B> {
	public final A first;
	public final B second;

	private Tuple(A first, B second) {
		this.first = first;
		this.second = second;
	}

	// Term introduction
	public static <A, B> Tuple<A, B> of(A first, B second) {
		return new Tuple<>(first, second);
	}

	// Term elimination
	public A getFirst() {
		return first;
	}

	public B getSecond() {
		return second;
	}

	public <T> Tuple<T, B> setFirst(T first) {
		return of(first, second);
	}

	public <T> Tuple<A, T> setSecond(T second) {
		return of(first, second);
	}

	// Java
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Tuple<?, ?> tuple = (Tuple<?, ?>) o;
		return Objects.equals(first, tuple.first) && Objects.equals(second, tuple.second);
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}

	@Override
	public String toString() {
		return "Tuple{" +
			"first=" + first +
			", second=" + second +
			'}';
	}
}
