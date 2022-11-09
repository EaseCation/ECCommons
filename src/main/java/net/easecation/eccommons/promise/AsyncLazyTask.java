package net.easecation.eccommons.promise;

@FunctionalInterface
public interface AsyncLazyTask<A, T> {
	void runAsync(A argument, AsyncHandler<T> handler);
}
