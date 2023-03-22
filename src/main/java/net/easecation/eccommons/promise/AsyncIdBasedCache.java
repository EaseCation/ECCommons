package net.easecation.eccommons.promise;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.TextFormat;
import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import net.easecation.eccommons.ECCommons;

import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AsyncIdBasedCache<A, T> {
	private final String taskName;
	private final AsyncIdBasedTask<A, T> task;
	private final Int2ObjectMap<T> cache;
	private final Int2ObjectMap<List<AsyncCallback<T>>> callbacks;

	public AsyncIdBasedCache(String taskName, AsyncIdBasedTask<A, T> task) {
		this.taskName = taskName;
		this.task = task;
		this.cache = new Int2ObjectOpenHashMap<>();
		this.callbacks = new Int2ObjectOpenHashMap<>();
	}

	public AsyncPromise<List<Int2ObjectMap.Entry<T>>> cacheAll(Int2ObjectMap<A> keys) {
		return cacheAll(keys.keySet(), keys::get);
	}

	public AsyncPromise<List<Int2ObjectMap.Entry<T>>> cacheAll(IntCollection keys, A argument) {
		return cacheAll(keys, key -> argument);
	}

	public AsyncPromise<List<Int2ObjectMap.Entry<T>>> cacheAll(IntCollection keys, IntFunction<A> arguments) {
		return AsyncPromise.awaitAll(keys.stream()
			.map(id -> acquireEntry(id, arguments.apply(id)))
			.collect(Collectors.toList())
		);
	}

	public AsyncPromise<Int2ObjectMap.Entry<T>> acquireEntry(int id, A argument) {
		return acquire(id, argument).map(value -> new AbstractInt2ObjectMap.BasicEntry<>(id, value));
	}

	public AsyncPromise<T> acquire(int id, A argument) {
		AsyncPromise<T> promise = AsyncPromise.pending();
		acquire(id, argument, promise);
		return promise;
	}

	public void acquire(int id, A argument, AsyncCallback<T> callback) {
		T value = cache.get(id);
		if (value != null) {
			try {
				if (callback != null) callback.onSuccess(value);
			} catch (Throwable e) {
				ECCommons.getInstance().getLogger().debug(TextFormat.RED + "调用异步任务 " + taskName + " 回调函数时发生错误 id = " + id, e);
			}
			return;
		}
		boolean newTask = false;
		List<AsyncCallback<T>> callbackList = callbacks.get(id);
		if (callbackList == null) {
			callbackList = new ArrayList<>();
			callbacks.put(id, callbackList);
			newTask = true;
		}
		if (callback != null) callbackList.add(callback);
		if (newTask) {
			final class InternalTask extends AsyncTask implements AsyncHandler<T> {
				final Deque<Runnable> syncTasks = new LinkedList<>();
				boolean hasResult = false;
				T result;

				@Override
				public void runSync(Runnable task) {
					this.syncTasks.offer(task);
				}

				@Override
				public void handle(T result) {
					this.hasResult = true;
					this.result = result;
				}

				@Override
				public void onRun() {
					try {
						task.runAsync(id, argument, this);
					} catch (Throwable e) {
						ECCommons.getInstance().getLogger().debug(TextFormat.RED + "运行异步任务 " + taskName + " 时发生错误 id = " + id);
					}
				}

				@Override
				public void onCompletion(Server server) {
					Runnable task;
					while ((task = this.syncTasks.poll()) != null) {
						try {
							task.run();
						} catch (Throwable e) {
							ECCommons.getInstance().getLogger().debug(TextFormat.RED + "完成异步任务 " + taskName + " 时发生错误 id = " + id);
						}
					}
					if (hasResult) {
						success(id, result);
					} else {
						failed(id);
					}
				}
			}
			ECCommons.getInstance().getLogger().debug(TextFormat.GREEN + "创建新的异步任务 " + taskName + " id = " + id);
			Server.getInstance().getScheduler().scheduleAsyncTask(ECCommons.getInstance(), new InternalTask());
		} else {
			ECCommons.getInstance().getLogger().debug(TextFormat.GREEN + "等待异步任务 " + taskName + " 完成中 id = " + id);
		}
	}

	private void success(int id, T entry) {
		cache.put(id, entry);
		List<AsyncCallback<T>> callbackList = callbacks.remove(id);
		if (callbackList == null) return;
		for (AsyncCallback<T> callback : callbackList) {
			try {
				if (callback != null) callback.onSuccess(entry);
			} catch (Throwable e) {
				ECCommons.getInstance().getLogger().debug(TextFormat.RED + "调用异步任务 " + taskName + " 回调函数时发生错误 id = " + id, e);
			}
		}
		ECCommons.getInstance().getLogger().debug(TextFormat.GREEN + "异步任务 " + taskName + " 完成 id = " + id);
	}

	private void failed(int id) {
		List<AsyncCallback<T>> callbackList = callbacks.remove(id);
		if (callbackList == null) return;
		for (AsyncCallback<T> callback : callbackList) {
			try {
				if (callback != null) callback.onFailed();
			} catch (Throwable e) {
				ECCommons.getInstance().getLogger().debug(TextFormat.RED + "调用异步任务 " + taskName + " 回调函数时发生错误 id = " + id, e);
			}
		}
		ECCommons.getInstance().getLogger().debug(TextFormat.RED + "异步任务 " + taskName + " 失败 id = " + id);
	}

	public Optional<T> get(int id) {
		return Optional.ofNullable(cache.get(id));
	}

	public T remove(int id) {
		return cache.remove(id);
	}

	public void clear() {
		cache.clear();
	}

	public Collection<T> entries() {
		return new ArrayList<>(cache.values());
	}

	public Stream<T> stream() {
		return cache.values().stream();
	}

	public int size() {
		return cache.size();
	}
}
