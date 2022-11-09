package net.easecation.eccommons.promise;

public interface AsyncHandler<T> {
	void runSync(Runnable task);

	void handle(T result);
}
