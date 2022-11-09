package net.easecation.eccommons.promise;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.TextFormat;
import net.easecation.eccommons.ECCommons;

import java.util.Deque;
import java.util.LinkedList;

public final class AsyncTransientScheduler<T> {
	private final String taskName;
	private final AsyncTransientTask<T> task;

	public AsyncTransientScheduler(String taskName, AsyncTransientTask<T> task) {
		this.taskName = taskName;
		this.task = task;
	}

	public AsyncPromise<T> schedule() {
		AsyncPromise<T> promise = AsyncPromise.pending();
		schedule(promise);
		return promise;
	}

	public void schedule(AsyncCallback<T> callback) {
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
					task.runAsync(this);
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
					success(callback, result);
				} else {
					failed(callback);
				}
			}
		}
		Server.getInstance().getScheduler().scheduleAsyncTask(ECCommons.getInstance(), new InternalTask());
		ECCommons.getInstance().getLogger().info(TextFormat.GREEN + "创建新的异步任务 " + taskName);
	}

	private void success(AsyncCallback<T> callback, T result) {
		try {
			if (callback != null) callback.onSuccess(result);
		} catch (Exception e) {
			ECCommons.getInstance().getLogger().alert(TextFormat.RED + "调用异步任务 " + taskName + " 回调函数时发生错误", e);
		}
		ECCommons.getInstance().getLogger().info(TextFormat.GREEN + "异步任务 " + taskName + " 完成");
	}

	private void failed(AsyncCallback<T> callback) {
		try {
			if (callback != null) callback.onFailed();
		} catch (Exception e) {
			ECCommons.getInstance().getLogger().alert(TextFormat.RED + "调用异步任务 " + taskName + " 回调函数时发生错误", e);
		}
		ECCommons.getInstance().getLogger().warn(TextFormat.RED + "异步任务 " + taskName + " 失败");
	}
}
