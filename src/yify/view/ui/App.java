package yify.view.ui;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.desktop.AppReopenedEvent;
import java.awt.desktop.AppReopenedListener;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import java.awt.desktop.QuitStrategy;
import javax.swing.ImageIcon;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import yify.model.api.yts.ConnectionThread;
import yify.model.api.yts.YTS_API;
import yify.model.torrentclient.TorrentClient;

public class App extends Application implements QuitHandler, AppReopenedListener {
	private static Stage primaryStage;
	private static YTS_API instance;
	private static ScrollPane mainContent;
	private static StackPane root;
	private static TitlePnl titlePnl;
	private static BrowserPnl browserPnl;
	private static BufferBarPnl bufferBarPnl;

	@Override
	public void start(Stage primaryStage) {
		App.primaryStage = primaryStage;
		Platform.setImplicitExit(false);

		titlePnl = new TitlePnl();
		browserPnl = new BrowserPnl();
		instance = YTS_API.instance();
		VBox mainContainer = new VBox(0, titlePnl, browserPnl);
		mainContainer.setBackground(new Background(new BackgroundFill(Color.rgb(29, 29, 29, 1f), null, null)));

		bufferBarPnl = new BufferBarPnl();
		bufferBarPnl.setVisible(false);

		/*********************** Setting Scene for BrowserPnl START *******/

		root = new StackPane();
		mainContent = new ScrollPane();

		mainContent.setBackground(new Background(new BackgroundFill(Color.rgb(29, 29, 29, 1f), null, null)));
		mainContent.setFitToWidth(true);
		mainContent.setFitToHeight(true);
		mainContent.setContent(mainContainer);

		root.getChildren().addAll(mainContent, bufferBarPnl);

		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		root.setPrefSize(size.getWidth() / 1.1, size.getHeight() / 1.1); // Take up most of but not the whole screen

		Scene scene = new Scene(root);
		scene.setFill(Color.rgb(29, 29, 29, 1f)); // The plain background is grey

		scene.getStylesheets().addAll("File:CSS/style.css", "File:CSS/transparentStage.css"); // Loading in the css
		primaryStage.setTitle("YIFY-Desktop");
		primaryStage.setScene(scene);

		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
			Desktop desktop = Desktop.getDesktop();
			desktop.setQuitHandler(this);
			desktop.setQuitStrategy(QuitStrategy.NORMAL_EXIT);
			desktop.addAppEventListener(this);
		}

		primaryStage.setOnCloseRequest(windowEvent -> {
			windowEvent.consume();
			primaryStage.hide();
			if (TorrentClient.getNumRunningTasks() == 0) {
				TorrentClient.quit();
				if (instance != null)
					ConnectionThread.kill();
				Platform.exit();
				System.exit(0);
			}
		});

		/*********************** Setting Scene for browserPnl END *********/

		// Trick to avoid blank stage for a second before displaying content.
		Thread trick = new Thread(new Runnable() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						primaryStage.setOpacity(0);
						primaryStage.show();
					}
				});

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						Timeline timeline = new Timeline();
						KeyFrame key = new KeyFrame(Duration.millis(100),
								new KeyValue(primaryStage.opacityProperty(), 1));
						timeline.getKeyFrames().add(key);
						timeline.play();
					}
				});

			}

		});
		trick.start();

	}

	@SuppressWarnings("exports")
	@Override
	public void handleQuitRequestWith(QuitEvent e, QuitResponse response) {
		System.out.println("Handled quit request");
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (TorrentClient.getNumRunningTasks() != 0) {
					Alert alert = new Alert(
							AlertType.WARNING, "Are you sure you want to quit?\nYou currently have "
									+ TorrentClient.getNumRunningTasks() + " running task(s).",
							ButtonType.YES, ButtonType.CANCEL);
					alert.getDialogPane().setStyle("fx-base: rgba(0,0,0,1)");
					alert.setOnCloseRequest(dialogEvent -> {
						if (alert.getResult() == ButtonType.YES) {
							Platform.exit();
							TorrentClient.quit();
							if (instance != null)
								ConnectionThread.kill();
							response.performQuit();

						} else {
							response.cancelQuit();
						}
					});

					alert.showAndWait();
				} else {
					System.out.println("exited");
					TorrentClient.quit();
					if (instance != null)
						ConnectionThread.kill();
					Platform.exit();
					response.performQuit();
				}
			}
		});
	}

	@SuppressWarnings("exports")
	@Override
	public void appReopened(AppReopenedEvent e) {
		System.out.println("Reopened");
		if (!primaryStage.isShowing()) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					primaryStage.setOpacity(0);
					primaryStage.show();
					Timeline timeline = new Timeline();
					KeyFrame key = new KeyFrame(Duration.millis(100), new KeyValue(primaryStage.opacityProperty(), 1));
					timeline.getKeyFrames().add(key);
					timeline.play();
				}
			});
		}
	}

	public static void showBufferBar() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				// Set the buffering bar to visible here
				bufferBarPnl.setVisible(true);
			}
		});
	}

	public static void hideBufferBar() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				bufferBarPnl.setVisible(false);
			}
		});
	}

	public static void switchSceneContent(Node node) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				((VBox) mainContent.getContent()).getChildren().set(1, node == null ? browserPnl : node);
			}
		});
	}

	public static BrowserPnl getBrowserPnl() {

		return browserPnl;
	}
	
	public static Window getStage() {
		return primaryStage;
	}

	public static void main(String[] args) {

		try {
			final Taskbar taskbar = Taskbar.getTaskbar();
			taskbar.setIconImage(new ImageIcon("assets/icon.png").getImage());
		} catch (Exception e) {
			// Won't work on Windows or Linux.
		}

		launch(args);
	}

}
