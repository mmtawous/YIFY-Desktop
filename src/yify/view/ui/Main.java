package yify.view.ui;

import java.awt.Taskbar;

import javax.swing.ImageIcon;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import yify.model.moviecatalog.MovieCatalog;

public class Main extends Application {
	private static MovieCatalog instance;
	private static ScrollPane root;
	private static BrowserPnl browserPnl;

	@Override
	public void start(Stage primaryStage) {
		// Constructing the MovieCatalog early will ensure the connection is checked early on
		instance = MovieCatalog.instance();
		browserPnl = new BrowserPnl();

		/*********************** Setting Scene for movieBrowser START *******/

		root = new ScrollPane();
		root.setBackground(new Background(new BackgroundFill(Color.rgb(29, 29, 29, 1f), null, null)));
		root.setFitToWidth(true);
		root.setFitToHeight(true);
		root.setContent(browserPnl);

		Scene scene = new Scene(root);
		
		scene.getStylesheets().add("File:CSS/style.css");
		primaryStage.setTitle("YIFIY-Scrape");
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent e) {
				Platform.exit();

				if (instance != null)
					instance.connectionThread.kill();

				System.exit(0);
			}
		});

		/*********************** Setting Scene for browserPnl END *********/

	}
	
	public static void switchSceneContent(Node node) {
		root.setContent(node);
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

	public static Node getBrowserPnl() {
		
		return browserPnl;
	}
}
