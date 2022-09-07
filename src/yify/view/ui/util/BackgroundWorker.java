package yify.view.ui.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundWorker {
	private static ExecutorService executor = Executors.newCachedThreadPool();
	
	public static void submit(Runnable runnable) {
		executor.submit(runnable);
	}

}
