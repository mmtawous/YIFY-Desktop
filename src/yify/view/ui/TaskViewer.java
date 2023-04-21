package yify.view.ui;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.controlsfx.control.TaskProgressView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import yify.model.api.yts.YTS_API;
import yify.model.torrentclient.SizeConvertCellFactory;
import yify.model.torrentclient.TorrentClient;

public class TaskViewer {
	private static Stage stage;
	private static TaskProgressView<DownloadTask> progressView;
	private static ExecutorService executorService = Executors.newCachedThreadPool();

	public static void show() {
		if (stage == null) {
			stage = new Stage();
			stage.setTitle("Download Progress");

			progressView = new TaskProgressView<>();
			progressView.setRetainTasks(true);

			Scene scene = new Scene(progressView);
			scene.setFill(Color.rgb(41, 41, 41));
			scene.getStylesheets().add("file:CSS/style.css");
			stage.setScene(scene);
			stage.show();
		} else {
			stage.show();
			stage.toFront();
		}

	}

	public static void addTask(String torrentName, String infoServerUrl, Process p, long downloadSize,
			String fileName) {
		DownloadTask taskToAdd = new DownloadTask(torrentName, infoServerUrl, p, downloadSize, fileName);

		// show() must be ran before adding because the progressView may not have been
		// constructed yet.
		show();
		executorService.submit(taskToAdd);
		progressView.getTasks().add(taskToAdd);

	}

	/**
	 * This method must first find the correct task to add the infoServerUrl to
	 * using the provided process parameter.
	 * 
	 * @param infoServerUrl the infoServerUrl to be submitted to the task executor
	 *                      thread.
	 * @param p             the process associated with the infoServerUrl
	 */
	public static void submitInfoServerUrl(String infoServerUrl, Process p) {
		ObservableList<DownloadTask> tasks = progressView.getTasks();
		for (int i = 0; i < tasks.size(); i++) {
			// using == comparison because we are looking at the object itself not its
			// contents.
			if (tasks.get(i).getProcess() == p) {
				tasks.get(i).infoServerUrl = infoServerUrl;
				break; // breaking as soon as the correct process is found
			}
		}
	}

	private static class DownloadTask extends Task<Void> {
		private volatile String infoServerUrl;
		private final Process process;
		private HttpClient client = YTS_API.getClient();
		private String downloadSpeed;
		private long downloadSize;
		private long downloaded;
		private String uploaded;
		private String runTime;
		private String timeEstimate;
		private String peers;

		public DownloadTask(String movieTitle, String infoServerUrl, Process p, long downloadSize, String fileName) {
			updateTitle(movieTitle + (fileName == null ? "" : " – " + fileName));
			if (downloadSize != 0) {
				this.downloadSize = downloadSize;
			} else {
				throw new IllegalArgumentException();
			}

			this.infoServerUrl = infoServerUrl;
			this.process = p;

			this.setOnCancelled(workerStateEvent -> {
				TorrentClient.stop(p);
				updateMessage("Cancelled");
			});

			this.setOnFailed(workerStateEvent -> {
				updateMessage("Failed – an uncaught exception occured");
				this.getException().printStackTrace();
				TorrentClient.stop(p);
			});

			this.setOnSucceeded(workerStateEvent -> {
				updateMessage("Completed – downloaded " + SizeConvertCellFactory.humanReadableByteCountSI(downloaded)
						+ (runTime == null ? " instantly" : " in " + runTime));
			});
		}

		@Override
		protected Void call() throws Exception {

			// This is not the best approach you should loop and catch any exceptions that
			// thrown until the program comes online.

//			Thread.sleep(1000); // 1000 is the delay to give a chance to the
//								// webtorrent-cli client server to come online

			getProgressDetails();
			while (!isCancelled() && downloaded < downloadSize) {
				Thread.sleep(500);

				// Checking if the stage is showing, if it is continue, otherwise sleep for a
				// second and return to save resources.
				while (!stage.isShowing()) {
					Thread.sleep(1000);
				}

				updateMessage(constructMessage());
				updateProgress(downloaded, downloadSize);
			}
			if (!isCancelled() && downloaded >= downloadSize && downloaded > 0) {
				this.succeeded();
			}

			return null;
		}

		private Process getProcess() {
			return process;
		}

		private void getProgressDetails() {
			HttpResponse<String> response = null;

			HttpRequest request = null;
			try {
				// Timeout after 20 seconds of waiting for info server
				long startTime = System.currentTimeMillis();
				while (infoServerUrl == null) {
					if (System.currentTimeMillis() > startTime + 20000)
						this.failed();
					else
						Thread.onSpinWait();
				}
				// Create a request
				request = HttpRequest.newBuilder(new URI(infoServerUrl)).timeout(Duration.ofSeconds(10)).build();
				// Use the client to send the request
				response = client.send(request, BodyHandlers.ofString());
			} catch (ConnectException e) {
				if (downloaded >= downloadSize || downloaded == 0) {
					downloaded = downloadSize;
					updateProgress(downloadSize, downloadSize);
					this.succeeded();
				}
				e.printStackTrace();
				return;
			} catch (IOException | InterruptedException | URISyntaxException e) {

				// It's okay if Interrupted so do nothing.
			}

			// the response:
			String jsonString = "";
			if (response != null)
				jsonString = response.body();
			else
				// Should hopefully never happen.
				return;

			JsonObject jsonResponse = JsonParser.parseString(jsonString).getAsJsonObject();
			this.downloadSpeed = jsonResponse.get("speed").getAsString();
			this.downloaded = jsonResponse.get("downloaded").getAsLong();
			this.uploaded = jsonResponse.get("uploaded").getAsString();
			this.runTime = jsonResponse.get("runtime").getAsString();
			this.timeEstimate = jsonResponse.get("estimate").getAsString();
			this.peers = jsonResponse.get("peers").getAsString();
		}

		private String constructMessage() {
			getProgressDetails();

			String message = "Speed: " + downloadSpeed + " | Downloaded "
					+ SizeConvertCellFactory.humanReadableByteCountSI(downloaded) + " of "
					+ SizeConvertCellFactory.humanReadableByteCountSI(downloadSize) + " | Uploaded: " + uploaded
					+ "\nTime running: " + runTime + " | Time remaining: " + timeEstimate + " | Peers: " + peers;

			return message;

		}

	}

}
