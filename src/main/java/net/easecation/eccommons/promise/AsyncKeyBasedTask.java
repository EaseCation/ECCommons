package net.easecation.eccommons.promise;

@FunctionalInterface
public interface AsyncKeyBasedTask<K, A, T> {
	void runAsync(K key, A argument, AsyncHandler<T> handler);
}
