package net.easecation.eccommons.promise;

import cn.nukkit.utils.TextFormat;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.easecation.eccommons.ECCommons;
import net.easecation.eccommons.adt.Either;
import net.easecation.eccommons.adt.Maybe;
import net.easecation.eccommons.adt.Tuple;
import net.easecation.eccommons.adt.Unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class AsyncPromise<T> implements AsyncCallback<T> {
	private final List<Consumer<T>> whenSuccess = new ArrayList<>();
	private final List<Runnable> whenFailed = new ArrayList<>();

	private boolean completed;
	private boolean success;
	private T value;

	private AsyncPromise(boolean completed, boolean success, T value) {
		this.completed = completed;
		this.success = success;
		this.value = value;
	}

	public boolean isCompleted() {
		return completed;
	}

	public boolean isSuccess() {
		return success;
	}

	public static <T> AsyncPromise<T> pending() {
		return new AsyncPromise<>(false, false, null);
	}

	public static <T> AsyncPromise<T> success(T value) {
		return new AsyncPromise<>(true, true, value);
	}

	public static <T> AsyncPromise<T> failed() {
		return new AsyncPromise<>(true, false, null);
	}

	public AsyncPromise<T> whenSuccess(Consumer<T> callback) {
		if (completed) {
			if (success) {
				try {
					callback.accept(value);
				} catch (Throwable e) {
					ECCommons.getInstance().getLogger().alert(TextFormat.RED + "调用回调函数时发生错误", e);
				}
			}
		} else {
			this.whenSuccess.add(callback);
		}
		return this;
	}

	public AsyncPromise<T> whenFailed(Runnable callback) {
		if (completed) {
			if (!success) {
				try {
					callback.run();
				} catch (Throwable e) {
					ECCommons.getInstance().getLogger().alert(TextFormat.RED + "调用回调函数时发生错误", e);
				}
			}
		} else {
			this.whenFailed.add(callback);
		}
		return this;
	}

	public AsyncPromise<T> whenCompleted(Runnable callback) {
		return whenSuccess(value -> callback.run()).whenFailed(callback);
	}

	/**
	 * Forward current promise to another pending promise.
	 * Target promise succeed if current promise succeed.
	 * Target promise failed if current promise failed.
	 */
	public AsyncPromise<T> forward(AsyncPromise<T> promise) {
		whenSuccess(promise::onSuccess);
		whenFailed(promise::onFailed);
		return this;
	}

	/**
	 * Run another promise when current promise completed whether succeed or failed.
	 * Promise succeed if another promise succeed.
	 * Promise failed if another promise failed.
	 */
	public <U> AsyncPromise<U> seq(Supplier<AsyncPromise<U>> f) {
		AsyncPromise<U> promise = pending();
		whenCompleted(() -> {
			AsyncPromise<U> newPromise;
			try {
				newPromise = f.get();
			} catch (Throwable e) {
				ECCommons.getInstance().getLogger().alert(TextFormat.RED + "转发代理Promise时发生错误", e);
				promise.onFailed();
				return;
			}
			newPromise.forward(promise);
		});
		return promise;
	}

	/**
	 * Replace with new value if current promise failed.
	 * Promise succeed if current promise succeed.
	 * Promise will not fail.
	 */
	public AsyncPromise<T> orElse(T value) {
		AsyncPromise<T> promise = AsyncPromise.pending();
		whenSuccess(promise::onSuccess);
		whenFailed(() -> promise.onSuccess(value));
		return promise;
	}

	/**
	 * Replace with new value if current promise failed.
	 * Promise succeed if current promise succeed.
	 * Promise will fail if supplier throw exception.
	 */
	public AsyncPromise<T> orElseGet(Supplier<T> f) {
		AsyncPromise<T> promise = AsyncPromise.pending();
		whenSuccess(promise::onSuccess);
		whenFailed(() -> {
			T newValue;
			try {
				newValue = f.get();
			} catch (Throwable e) {
				ECCommons.getInstance().getLogger().alert(TextFormat.RED + "计算新Promise值时发生错误", e);
				promise.onFailed();
				return;
			}
			promise.onSuccess(newValue);
		});
		return promise;
	}

	/**
	 * Replace with nothing if current promise failed.
	 * Promise succeed if current promise succeed.
	 * Promise will not fail.
	 */
	public AsyncPromise<Maybe<T>> orElseMaybe() {
		AsyncPromise<Maybe<T>> promise = AsyncPromise.pending();
		whenSuccess(value -> promise.onSuccess(Maybe.ofJust(value)));
		whenFailed(() -> promise.onSuccess(Maybe.ofNothing()));
		return promise;
	}

	/**
	 * Await all promise to complete.
	 * Promise succeed if some succeed.
	 * Promise will not fail.
	 */
	public static <T> AsyncPromise<List<T>> awaitAll(Collection<AsyncPromise<T>> tasks) {
		return awaitAll(tasks, false);
	}

	/**
	 * Await all promise to complete.
	 * <p>
	 * If {@code strict} is {@code true}.
	 * Promise succeed if all succeed.
	 * Promise failed if any failed.
	 * <p>
	 * If {@code strict} is {@code false}.
	 * Promise succeed if some succeed.
	 * Promise will not fail.
	 */
	public static <T> AsyncPromise<List<T>> awaitAll(Collection<AsyncPromise<T>> tasks, boolean strict) {
		if (tasks.isEmpty()) return success(new ArrayList<>());
		AsyncPromise<List<T>> promise = pending();
		new Object() {
			final int numTask = tasks.size();
			final List<T> results = new ArrayList<>(numTask);
			int completedTask = 0;
			boolean failed = false;

			public void run() {
				for (AsyncPromise<T> task : tasks) {
					task.whenSuccess(result -> {
						completedTask++;
						results.add(result);
						if (failed) return;
						if (completedTask >= numTask) promise.onSuccess(results);
					});
					task.whenFailed(() -> {
						completedTask++;
						if (failed) return;
						if (strict) {
							failed = true;
							promise.onFailed();
							return;
						}
						if (completedTask >= numTask) promise.onSuccess(results);
					});
				}
			}
		}.run();
		return promise;
	}

	public static <T> AsyncPromise<List<T>> awaitAllSeq(Collection<AsyncPromise<T>> tasks) {
		return awaitAllSeq(tasks, false);
	}

	public static <T> AsyncPromise<List<T>> awaitAllSeq(Collection<AsyncPromise<T>> tasks, boolean strict) {
		if (tasks.isEmpty()) return success(new ArrayList<>());
		AsyncPromise<List<T>> promise = pending();
		new Object() {
			final int numTask = tasks.size();
			final Int2ObjectMap<T> results = new Int2ObjectOpenHashMap<>(numTask);
			int completedTask = 0;
			boolean failed = false;

			public void run() {
				int i = 0;
				for (AsyncPromise<T> task : tasks) {
					int index = i++;
					task.whenSuccess(result -> {
						completedTask++;
						results.put(index, result);
						if (failed) return;
						if (completedTask >= numTask) {
							List<T> list = new ArrayList<>(numTask);
							for (int j = 0; j < numTask; j++) {
								list.add(results.get(j));
							}
							promise.onSuccess(list);
						}
					});
					task.whenFailed(() -> {
						completedTask++;
						if (failed) return;
						if (strict) {
							failed = true;
							promise.onFailed();
							return;
						}
						if (completedTask >= numTask) promise.onSuccess(new ArrayList<>(results.values()));
					});
				}
			}
		}.run();
		return promise;
	}

	public static <T, U> AsyncPromise<List<U>> awaitAllFlatMap(Collection<AsyncPromise<T>> tasks, Function<T, List<U>> mapper) {
		return awaitAllFlatMap(tasks, mapper, false);
	}

	public static <T, U> AsyncPromise<List<U>> awaitAllFlatMap(Collection<AsyncPromise<T>> tasks, Function<T, List<U>> mapper, boolean strict) {
		if (tasks.isEmpty()) return success(new ArrayList<>());
		AsyncPromise<List<U>> promise = pending();
		new Object() {
			final int numTask = tasks.size();
			final List<U> results = new ArrayList<>();
			int completedTask = 0;
			boolean failed = false;

			public void run() {
				for (AsyncPromise<T> task : tasks) {
					task.whenSuccess(result -> {
						completedTask++;
						results.addAll(mapper.apply(result));
						if (failed) return;
						if (completedTask >= numTask) promise.onSuccess(results);
					});
					task.whenFailed(() -> {
						completedTask++;
						if (failed) return;
						if (strict) {
							failed = true;
							promise.onFailed();
							return;
						}
						if (completedTask >= numTask) promise.onSuccess(results);
					});
				}
			}
		}.run();
		return promise;
	}

	/**
	 * Await all promise to complete passively.
	 * Promise succeed if some succeed.
	 * Promise will not fail.
	 */
	public static <T> AsyncPromise<Unit> awaitAllPassive(Collection<AsyncPromise<T>> tasks) {
		return awaitAllPassive(tasks, false);
	}

	/**
	 * Await all promise to complete passively.
	 * <p>
	 * If {@code strict} is {@code true}.
	 * Promise succeed if all succeed.
	 * Promise failed if any failed.
	 * <p>
	 * If {@code strict} is {@code false}.
	 * Promise succeed if some succeed.
	 * Promise will not fail.
	 */
	public static <T> AsyncPromise<Unit> awaitAllPassive(Collection<AsyncPromise<T>> tasks, boolean strict) {
		if (tasks.isEmpty()) return success(Unit.UNIT);
		AsyncPromise<Unit> promise = pending();
		new Object() {
			final int numTask = tasks.size();
			int completedTask = 0;
			boolean failed = false;

			public void run() {
				for (AsyncPromise<T> task : tasks) {
					task.whenSuccess(result -> {
						completedTask++;
						if (failed) return;
						if (completedTask >= numTask) promise.onSuccess(Unit.UNIT);
					});
					task.whenFailed(() -> {
						completedTask++;
						if (failed) return;
						if (strict) {
							failed = true;
							promise.onFailed();
							return;
						}
						if (completedTask >= numTask) promise.onSuccess(Unit.UNIT);
					});
				}
			}
		}.run();
		return promise;
	}

	/**
	 * Await any promise to complete.
	 * Promise succeed if any succeed.
	 * Promise failed if any failed.
	 */
	public static <T> AsyncPromise<T> awaitAny(Collection<AsyncPromise<T>> tasks) {
		AsyncPromise<T> promise = pending();
		new Object() {
			boolean completed = false;

			public void run() {
				for (AsyncPromise<T> task : tasks) {
					task.whenSuccess(result -> {
						if (completed) return;
						completed = true;
						promise.onSuccess(result);
					});
					task.whenFailed(() -> {
						if (completed) return;
						completed = true;
						promise.onFailed();
					});
				}
			}
		}.run();
		return promise;
	}

	/**
	 * Await some promise to complete.
	 * Promise succeed if any succeed.
	 * Promise failed if all failed.
	 */
	public static <T> AsyncPromise<T> awaitSome(Collection<AsyncPromise<T>> tasks) {
		AsyncPromise<T> promise = pending();
		new Object() {
			final int numTask = tasks.size();
			int completedTask = 0;
			boolean completed = false;

			public void run() {
				for (AsyncPromise<T> task : tasks) {
					task.whenSuccess(result -> {
						completedTask++;
						if (completed) return;
						completed = true;
						promise.onSuccess(result);
					});
					task.whenFailed(() -> {
						completedTask++;
						if (completed) return;
						if (completedTask >= numTask) {
							completed = true;
							promise.onFailed();
						}
					});
				}
			}
		}.run();
		return promise;
	}

	/**
	 * Await two promise to complete.
	 * Promise succeed if all succeed.
	 * Promise failed if any failed.
	 */
	public static <T, U> AsyncPromise<Tuple<T, U>> awaitTwo(AsyncPromise<T> first, AsyncPromise<U> second) {
		AsyncPromise<List<Either<T, U>>> promise = awaitAll(Arrays.asList(first.map(Either::ofLeft), second.map(Either::ofRight)), true);
		return promise.flatMap(result -> {
			if (result.size() != 2) return AsyncPromise.failed();
			Either<T, U> firstResult = result.get(0);
			Either<T, U> secondResult = result.get(1);
			if (firstResult.isLeft() && secondResult.isLeft()) return AsyncPromise.failed();
			if (firstResult.isRight() && secondResult.isRight()) return AsyncPromise.failed();
			T firstValue = firstResult.isLeft() ? firstResult.coerceLeft() : secondResult.coerceLeft();
			U secondValue = firstResult.isRight() ? firstResult.coerceRight() : secondResult.coerceRight();
			return AsyncPromise.success(Tuple.of(firstValue, secondValue));
		});
	}

	/**
	 * Await either promise to complete.
	 * Promise succeed if any succeed.
	 * Promise failed if all failed.
	 */
	public static <T, U> AsyncPromise<Either<T, U>> awaitEither(AsyncPromise<T> left, AsyncPromise<U> right) {
		return awaitSome(Arrays.asList(left.map(Either::ofLeft), right.map(Either::ofRight)));
	}

	@Override
	public void onSuccess(T value) {
		if (completed) throw new IllegalStateException("Promise already completed");
		this.completed = true;
		this.success = true;
		this.value = value;
		for (Consumer<T> callback : this.whenSuccess) {
			try {
				callback.accept(value);
			} catch (Throwable e) {
				ECCommons.getInstance().getLogger().alert(TextFormat.RED + "调用回调函数时发生错误", e);
			}
		}
	}

	@Override
	public void onFailed() {
		if (completed) throw new IllegalStateException("Promise already completed");
		this.completed = true;
		this.success = false;
		for (Runnable callback : this.whenFailed) {
			try {
				callback.run();
			} catch (Throwable e) {
				ECCommons.getInstance().getLogger().alert(TextFormat.RED + "调用回调函数时发生错误", e);
			}
		}
	}

	// Monadic Interface
	public <U> AsyncPromise<U> map(Function<T, U> f) {
		AsyncPromise<U> promise = pending();
		whenSuccess(value -> {
			U newValue;
			try {
				newValue = f.apply(value);
			} catch (Throwable e) {
				ECCommons.getInstance().getLogger().alert(TextFormat.RED + "计算新Promise值时发生错误", e);
				promise.onFailed();
				return;
			}
			promise.onSuccess(newValue);
		});
		whenFailed(promise::onFailed);
		return promise;
	}

	public <U> AsyncPromise<U> flatMap(Function<T, AsyncPromise<U>> f) {
		AsyncPromise<U> promise = pending();
		whenSuccess(value -> {
			AsyncPromise<U> newPromise;
			try {
				newPromise = f.apply(value);
			} catch (Throwable e) {
				ECCommons.getInstance().getLogger().alert(TextFormat.RED + "转发代理Promise时发生错误", e);
				promise.onFailed();
				return;
			}
			newPromise.forward(promise);
		});
		whenFailed(promise::onFailed);
		return promise;
	}

	public AsyncPromise<T> filter(Predicate<T> p) {
		return flatMap(value -> p.test(value) ? success(value) : failed());
	}

	public static <T> AsyncPromise<T> pure(T value) {
		return success(value);
	}
}
