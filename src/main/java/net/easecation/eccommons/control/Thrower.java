package net.easecation.eccommons.control;

import java.util.function.Supplier;

public interface Thrower<T extends Throwable> {
	<A> A apply() throws T;

	static <T extends Throwable> Thrower<T> of(Supplier<T> f) {
		return new Thrower<T>() { public <A> A apply() throws T { throw f.get(); } };
	}
}
