package yify.model.api.yts.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CloseableHttpClient implements Closeable {
	private HttpClient client;
	ExecutorService executor;

	/**
	 * Constructs a CloseableHttpClient given a builder with all user preferences
	 * already set. Note we set the executor in this constructor so it will be
	 * overwritten if the user already set it.
	 * 
	 * @param builder the builder to build this HttpClient with.
	 */
	public CloseableHttpClient(HttpClient.Builder builder) {

		// Initialize the executor this client will use
		this.executor = Executors.newSingleThreadExecutor();

		// Set the executor and build the client
		this.client = builder.executor(executor).build();
	}

	/**
	 * Returns the client
	 * 
	 * @return the HttpClient we are delegating to.
	 */
	public HttpClient getClient() {
		return client;
	}

	/**
	 * Close this client by shutting down its executor and nulling its client
	 * reference then running the garbage collector.
	 */
	@Override
	public void close() throws IOException {
		executor.shutdown();
		executor = null;
		client = null;
		System.gc();
	}

}
