package net.easecation.eccommons.promise;

@FunctionalInterface
public interface AsyncCallback<T> {
	void onSuccess(T value);

	default void onFailed() { }
}
