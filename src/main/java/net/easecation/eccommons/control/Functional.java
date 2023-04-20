package net.easecation.eccommons.control;

import java.util.function.Function;

public final class Functional {
	public static <T, R> R apply(Function<T, R> f, T value) {
		return f.apply(value);
	}

	public static <T, R> R apply(T value, Function<T, R> f) {
		return f.apply(value);
	}
}
