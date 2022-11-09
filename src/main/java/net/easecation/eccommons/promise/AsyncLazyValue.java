package net.easecation.eccommons.promise;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.TextFormat;
import net.easecation.eccommons.ECCommons;

import java.util.*;

public final class AsyncLazyValue<A, T> {
	private final String taskName;
	private final AsyncLazyTask<A, T> task;
	private boolean loaded;
	private T value;
	private List<AsyncCallback<T>> callbacks;

	public AsyncLazyValue(String taskName, AsyncLazyTask<A, T> task) {
		this.taskName = taskName;
		this.task = task;
		this.loaded = false;
		this.value = null;
		this.callbacks = null;
	}

	public AsyncPromise<T> load(A argument) {
		AsyncPromise<T> promise = AsyncPromise.pending();
		load(argument, promise);
		return promise;
	}

	public void load(A argument, AsyncCallback<T> callback) {
		if (loaded) {
			try {
				if (callback != null) callback.onSuccess(value);
			} catch (Exception e) {
				ECCommons.getInstance().getLogger().alert(TextFormat.RED + "调用异步任务 " + taskName + " 回调函数时发生错误", e);
			}
			return;
		}
		boolean newTask = false;
		if (callbacks == null) {
			callbacks = new ArrayList<>();
			newTask = true;
		}
		if (callback != null) callbacks.add(callback);
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
						task.runAsync(argument, this);
					} catch (Exception e) {
						ECCommons.getInstance().getLogger().alert(TextFormat.RED + "运行异步任务 " + taskName + " 时发生错误");
					}
				}

				@Override
				public void onCompletion(Server server) {
					Runnable task;
					while ((task = this.syncTasks.poll()) != null) {
						try {
							task.run();
						} catch (Exception e) {
							ECCommons.getInstance().getLogger().alert(TextFormat.RED + "完成异步任务 " + taskName + " 时发生错误");
						}
					}
					if (hasResult) {
						success(result);
					} else {
						failed();
					}
				}
			}
			Server.getInstance().getScheduler().scheduleAsyncTask(ECCommons.getInstance(), new InternalTask());
			ECCommons.getInstance().getLogger().info(TextFormat.GREEN + "创建新的异步任务 " + taskName);
		} else {
			ECCommons.getInstance().getLogger().info(TextFormat.GREEN + "等待异步任务 " + taskName + " 完成中");
		}
	}

	private void success(T value) {
		this.loaded = true;
		this.value = value;
		if (this.callbacks == null) return;
		for (AsyncCallback<T> callback : this.callbacks) {
			try {
				if (callback != null) callback.onSuccess(value);
			} catch (Exception e) {
				ECCommons.getInstance().getLogger().alert(TextFormat.RED + "调用异步任务 " + taskName + " 回调函数时发生错误", e);
			}
		}
		this.callbacks = null;
		ECCommons.getInstance().getLogger().info(TextFormat.GREEN + "异步任务 " + taskName + " 完成");
	}

	private void failed() {
		if (this.callbacks == null) return;
		for (AsyncCallback<T> callback : this.callbacks) {
			try {
				if (callback != null) callback.onFailed();
			} catch (Exception e) {
				ECCommons.getInstance().getLogger().alert(TextFormat.RED + "调用异步任务 " + taskName + " 回调函数时发生错误", e);
			}
		}
		this.callbacks = null;
		ECCommons.getInstance().getLogger().warn(TextFormat.RED + "异步任务 " + taskName + " 失败");
	}

	public Optional<T> get() {
		return loaded ? Optional.ofNullable(value) : Optional.empty();
	}

	public void expire() {
		this.loaded = false;
		this.value = null;
	}
}
