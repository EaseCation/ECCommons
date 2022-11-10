package net.easecation.eccommons.promise;

import cn.nukkit.utils.TextFormat;
import java.util.Arrays;
import net.easecation.eccommons.ECCommons;
import net.easecation.eccommons.adt.Either;
import net.easecation.eccommons.adt.Tuple;
import net.easecation.eccommons.adt.Unit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

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
				} catch (Exception e) {
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
				} catch (Exception e) {
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

	public static <T> AsyncPromise<List<T>> awaitAll(Collection<AsyncPromise<T>> tasks) {
		return awaitAll(tasks, false);
	}

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

	public static <T> AsyncPromise<Unit> awaitAllPassive(Collection<AsyncPromise<T>> tasks) {
		return awaitAllPassive(tasks, false);
	}

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

	public static <T> AsyncPromise<T> awaitSome(Collection<AsyncPromise<T>> tasks) {
		AsyncPromise<T> promise = pending();
		new Object() {
			final int numTask = tasks.size();
			int completedTask = 0;
			boolean failed = false;
			boolean succeed = false;

			public void run() {
				for (AsyncPromise<T> task : tasks) {
					task.whenSuccess(result -> {
						completedTask++;
						if (failed || succeed) return;
						succeed = true;
						promise.onSuccess(result);
					});
					task.whenFailed(() -> {
						completedTask++;
						if (failed || succeed) return;
						if (completedTask >= numTask) {
							failed = true;
							promise.onFailed();
						}
					});
				}
			}
		}.run();
		return promise;
	}

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
			} catch (Exception e) {
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
			} catch (Exception e) {
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
			} catch (Exception e) {
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
			} catch (Exception e) {
				ECCommons.getInstance().getLogger().alert(TextFormat.RED + "转发代理Promise时发生错误", e);
				promise.onFailed();
				return;
			}
			newPromise.whenSuccess(promise::onSuccess);
			newPromise.whenFailed(promise::onFailed);
		});
		whenFailed(promise::onFailed);
		return promise;
	}

	public static <T> AsyncPromise<T> pure(T value) {
		return success(value);
	}
}
