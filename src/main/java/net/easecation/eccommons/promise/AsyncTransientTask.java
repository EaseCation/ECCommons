package net.easecation.eccommons.promise;

@FunctionalInterface
public interface AsyncTransientTask<T> {
	void runAsync(AsyncHandler<T> handler);
}
