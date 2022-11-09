package net.easecation.eccommons.promise;

@FunctionalInterface
public interface AsyncIdBasedTask<A, T> {
	void runAsync(int id, A argument, AsyncHandler<T> handler);
}
