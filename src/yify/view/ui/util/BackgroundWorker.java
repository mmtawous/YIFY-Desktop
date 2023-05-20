package yify.view.ui.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BackgroundWorker {
	private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

	public static void submit(Runnable runnable) {
		executor.setKeepAliveTime(10, TimeUnit.SECONDS);
		executor.submit(runnable);
	}
	
	public static ThreadPoolExecutor getEx() {
		return executor;
	}
	

}
