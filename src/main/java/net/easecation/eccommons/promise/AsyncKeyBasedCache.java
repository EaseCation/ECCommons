package net.easecation.eccommons.promise;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.TextFormat;
import it.unimi.dsi.fastutil.objects.AbstractObject2ObjectMap;
import net.easecation.eccommons.ECCommons;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AsyncKeyBasedCache<K, A, T> {
	private final String taskName;
	private final AsyncKeyBasedTask<K, A, T> task;
	private final Map<K, T> cache;
	private final Map<K, List<AsyncCallback<T>>> callbacks;

	public AsyncKeyBasedCache(String taskName, AsyncKeyBasedTask<K, A, T> task) {
		this.taskName = taskName;
		this.task = task;
		this.cache = new HashMap<>();
		this.callbacks = new HashMap<>();
	}

	public AsyncPromise<List<Map.Entry<K, T>>> cacheAll(Map<K, A> keys) {
		return cacheAll(keys.keySet(), keys::get);
	}

	public AsyncPromise<List<Map.Entry<K, T>>> cacheAll(Collection<K> keys, A argument) {
		return cacheAll(keys, key -> argument);
	}

	public AsyncPromise<List<Map.Entry<K, T>>> cacheAll(Collection<K> keys, Function<K, A> arguments) {
		return AsyncPromise.awaitAll(keys.stream()
			.map(key -> acquireEntry(key, arguments.apply(key)))
			.collect(Collectors.toList())
		);
	}

	public AsyncPromise<Map.Entry<K, T>> acquireEntry(K key, A argument) {
		return acquire(key, argument).map(value -> new AbstractObject2ObjectMap.BasicEntry<>(key, value));
	}

	public AsyncPromise<T> acquire(K key, A argument) {
		AsyncPromise<T> promise = AsyncPromise.pending();
		acquire(key, argument, promise);
		return promise;
	}

	public void acquire(K key, A argument, AsyncCallback<T> callback) {
		T value = cache.get(key);
		if (value != null) {
			try {
				if (callback != null) callback.onSuccess(value);
			} catch (Exception e) {
				ECCommons.getInstance().getLogger().alert(TextFormat.RED + "调用异步任务 " + taskName + " 回调函数时发生错误 key = " + key, e);
			}
			return;
		}
		boolean newTask = false;
		List<AsyncCallback<T>> callbackList = callbacks.get(key);
		if (callbackList == null) {
			callbackList = new ArrayList<>();
			callbacks.put(key, callbackList);
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
						task.runAsync(key, argument, this);
					} catch (Exception e) {
						ECCommons.getInstance().getLogger().alert(TextFormat.RED + "运行异步任务 " + taskName + " 时发生错误 key = " + key);
					}
				}

				@Override
				public void onCompletion(Server server) {
					Runnable task;
					while ((task = this.syncTasks.poll()) != null) {
						try {
							task.run();
						} catch (Exception e) {
							ECCommons.getInstance().getLogger().alert(TextFormat.RED + "完成异步任务 " + taskName + " 时发生错误 key = " + key);
						}
					}
					if (hasResult) {
						success(key, result);
					} else {
						failed(key);
					}
				}
			}
			ECCommons.getInstance().getLogger().info(TextFormat.GREEN + "创建新的异步任务 " + taskName + " key = " + key);
			Server.getInstance().getScheduler().scheduleAsyncTask(ECCommons.getInstance(), new InternalTask());
		} else {
			ECCommons.getInstance().getLogger().info(TextFormat.GREEN + "等待异步任务 " + taskName + " 完成中 key = " + key);
		}
	}

	private void success(K key, T entry) {
		cache.put(key, entry);
		List<AsyncCallback<T>> callbackList = callbacks.remove(key);
		if (callbackList == null) return;
		for (AsyncCallback<T> callback : callbackList) {
			try {
				if (callback != null) callback.onSuccess(entry);
			} catch (Exception e) {
				ECCommons.getInstance().getLogger().alert(TextFormat.RED + "调用异步任务 " + taskName + " 回调函数时发生错误 key = " + key, e);
			}
		}
		ECCommons.getInstance().getLogger().info(TextFormat.GREEN + "异步任务 " + taskName + " 完成 key = " + key);
	}

	private void failed(K key) {
		List<AsyncCallback<T>> callbackList = callbacks.remove(key);
		if (callbackList == null) return;
		for (AsyncCallback<T> callback : callbackList) {
			try {
				if (callback != null) callback.onFailed();
			} catch (Exception e) {
				ECCommons.getInstance().getLogger().alert(TextFormat.RED + "调用异步任务 " + taskName + " 回调函数时发生错误 key = " + key, e);
			}
		}
		ECCommons.getInstance().getLogger().warn(TextFormat.RED + "异步任务 " + taskName + " 失败 key = " + key);
	}

	public Optional<T> get(K key) {
		return Optional.ofNullable(cache.get(key));
	}

	public T remove(K key) {
		return cache.remove(key);
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
