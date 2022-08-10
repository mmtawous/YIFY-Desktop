package yify.view.ui;

import java.io.File;
import java.io.IOException;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import yify.model.torrentclient.MovieFile;
import yify.model.torrentclient.SizeConvertCellFactory;
import yify.model.torrentclient.StreamType;
import yify.model.torrentclient.TorrentClient;

public class DownloadDialog {
	private static Stage stage;
	private static boolean isOpen;

	public static void show(String title, String torrentName, ObservableList<MovieFile> fileList, String torrentId) {
		if (isOpen) {
			stage.toFront();
			return;
		}

		stage = new Stage();
		stage.setTitle(title);
		stage.initStyle(StageStyle.UTILITY);
		isOpen = true;

		// Setting up root node.
		VBox root = new VBox();
		root.setAlignment(Pos.TOP_CENTER);
		root.setPadding(new Insets(50, 20, 0, 20));
		root.setBackground(new Background(new BackgroundFill(Color.rgb(40, 40, 40), CornerRadii.EMPTY, Insets.EMPTY)));

		/*************** Start Output File Path Selection Content ****************/
		Label saveTo = new Label("Save to: ");
		saveTo.setFont(new Font("Arial", 14));
		saveTo.setTextFill(Color.WHITE);
		saveTo.setPadding(new Insets(5, 10, 0, 0));

		// Custom prompt text based on OS
		TextField outFileTxtField = new TextField();
		outFileTxtField.setPromptText(getDefaultDownload());
		outFileTxtField.setEffect(new InnerShadow(5, 0, 2, Color.rgb(0, 0, 0, 0.30f)));
		outFileTxtField.setId("interactiveTextField");
		outFileTxtField.setPrefSize(300, 20);
		outFileTxtField.setFocusTraversable(false);

		// Initialize browse button which opens the directory chooser window
		ImageView menuIcon = new ImageView(new Image("file:assets/menuDots.png"));
		Button browseBtn = new Button("", menuIcon);
		// browseBtn.setEffect(new InnerShadow(5, -4, 4, Color.rgb(0, 0, 0, 0.20f)));
		browseBtn.setBackground(new Background(
				new BackgroundFill(Color.rgb(52, 52, 52), new CornerRadii(0, 5, 5, 0, false), Insets.EMPTY)));
		browseBtn.setPrefSize(25, 26);
		browseBtn.setFocusTraversable(false);
		browseBtn.setOnMousePressed(mouseEvent -> {
			browseBtn.setBackground(new Background(new BackgroundFill(Color.rgb(52, 52, 52).brighter(),
					new CornerRadii(0, 5, 5, 0, false), Insets.EMPTY)));
		});
		browseBtn.setOnMouseReleased(mouseEvent -> {
			browseBtn.setBackground(new Background(
					new BackgroundFill(Color.rgb(52, 52, 52), new CornerRadii(0, 5, 5, 0, false), Insets.EMPTY)));
		});

		browseBtn.setOnAction(actionEvent -> {
			// openFileChooser returns the output file path chosen by the user. If the user
			// clicks cancel or closes it then the result will be null, so the default
			// download directory is set instead.
			File outFile = openFileChooser();
			if (outFile != null)
				outFileTxtField.setText(outFile.getAbsolutePath());
			else
				outFileTxtField.setText(getDefaultDownload());
		});

		// Adding everything to a container node
		HBox outFileContent = new HBox(saveTo, outFileTxtField, browseBtn);
		outFileContent.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, Color.TRANSPARENT,
				Color.rgb(53, 53, 53), Color.TRANSPARENT, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE,
				BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE, CornerRadii.EMPTY, new BorderWidths(1), null)));

		outFileContent.setEffect(new DropShadow(5, 0, 2, Color.rgb(0, 0, 0, 0.20f)));
		outFileContent.setAlignment(Pos.TOP_LEFT);
		outFileContent.setPadding(new Insets(0, 0, 15, 0));

		// Adding the container node to the root node
		root.getChildren().add(outFileContent);

		/********** Start File To Download Selection Content ************/

		Label movieTitleLbl = new Label("Name: " + torrentName);
		movieTitleLbl.setFont(new Font("Arial", 14));
		movieTitleLbl.setTextFill(Color.WHITE);

		TableView<MovieFile> fileTable = new TableView<MovieFile>();
		fileTable.setEditable(false);
		fileTable.setPrefHeight(245);

		TableColumn<MovieFile, String> fileNameCol = new TableColumn<>("File Name");
		fileNameCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
		fileNameCol.setPrefWidth(400);
		fileNameCol.setResizable(false);

		TableColumn<MovieFile, Long> fileSizeCol = new TableColumn<>("Size");
		fileSizeCol.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
//		fileSizeCol.prefWidthProperty().bind(ReadOnlyDoubleProperty.doubleExpression(fileTable.widthProperty())
//				.subtract(fileNameCol.widthProperty()));
		fileSizeCol.setCellFactory(new SizeConvertCellFactory());
		fileSizeCol.setResizable(false);

		// Unselects all rows if an empty row is clicked. I don't know why this isn't
		// default :/
		fileTable.setRowFactory(new Callback<TableView<MovieFile>, TableRow<MovieFile>>() {
			@Override
			public TableRow<MovieFile> call(TableView<MovieFile> tableView2) {
				final TableRow<MovieFile> row = new TableRow<>();
				row.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (row.isEmpty()) {
							fileTable.getSelectionModel().clearSelection();
						}
					}
				});
				return row;
			}
		});

		fileTable.setItems(fileList);
		fileTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		fileTable.setOnMouseClicked(mouseEvent -> {

		});

		// Note on UI logic for user clarity.
		Label noteToUser = new Label(
				"**NOTE**\nNot selecting a file above will download all the files listed. You may opt to choose one file to download instead.");
		noteToUser.setFont(new Font("Arial-Bold", 12));
		noteToUser.setTextFill(Color.rgb(100, 100, 100));
		noteToUser.setWrapText(true);
		noteToUser.setPrefWidth(300);

		fileTable.getColumns().add(fileNameCol);
		fileTable.getColumns().add(fileSizeCol);

		VBox fileSelectContent = new VBox(5);
		fileSelectContent.setAlignment(Pos.TOP_LEFT);
		fileSelectContent.getChildren().addAll(movieTitleLbl, fileTable, noteToUser);
		fileSelectContent.setPadding(new Insets(15, 0, 0, 0));

		root.getChildren().add(fileSelectContent);

		/********** Download and Cancel Button Content ************/
		HBox downloadCancelContent = new HBox(15);
		downloadCancelContent.setAlignment(Pos.BOTTOM_RIGHT);
		downloadCancelContent.setPadding(new Insets(30, 0, 0, 0));

		Button cancelBtn = new Button("Cancel");
		cancelBtn.setFont(new Font("Arial", 14));
		cancelBtn.setTextFill(Color.WHITE);
		cancelBtn.setPadding(new Insets(5, 20, 5, 20));
		cancelBtn.setEffect(new DropShadow(5, 0.5, 0, Color.rgb(0, 0, 0, 0.25f)));
		cancelBtn.setBackground(
				new Background(new BackgroundFill(Color.rgb(95, 95, 95), new CornerRadii(5), Insets.EMPTY)));
		cancelBtn.setOnMousePressed(mouseEvent -> {
			cancelBtn.setBackground(
					new Background(new BackgroundFill(Color.rgb(119, 119, 119), new CornerRadii(5), Insets.EMPTY)));
		});
		cancelBtn.setOnMouseReleased(mouseEvent -> {
			cancelBtn.setBackground(
					new Background(new BackgroundFill(Color.rgb(95, 95, 95), new CornerRadii(5), Insets.EMPTY)));
		});
		cancelBtn.setOnAction(actionEvent -> {
			handleCancel();
		});

		Button downloadBtn = new Button("Download");
		downloadBtn.setFont(new Font("Arial", 14));
		downloadBtn.setTextFill(Color.WHITE);
		downloadBtn.setPadding(new Insets(5, 20, 5, 20));
		downloadBtn.setEffect(new DropShadow(5, 0.5, 0, Color.rgb(0, 0, 0, 0.25f)));
		downloadBtn.setBackground(
				new Background(new BackgroundFill(Color.rgb(38, 110, 205), new CornerRadii(5), Insets.EMPTY)));
		downloadBtn.setOnMousePressed(mouseEvent -> {
			downloadBtn.setBackground(
					new Background(new BackgroundFill(Color.rgb(61, 149, 231), new CornerRadii(5), Insets.EMPTY)));
		});
		downloadBtn.setOnMouseReleased(mouseEvent -> {
			downloadBtn.setBackground(
					new Background(new BackgroundFill(Color.rgb(38, 110, 205), new CornerRadii(5), Insets.EMPTY)));
		});
		downloadBtn.setOnAction(actionEvent -> {
			Integer selectFileIndex = null;

			if (fileTable.getSelectionModel().getSelectedItem() != null) {
				selectFileIndex = fileTable.getSelectionModel().getSelectedItem().getIndex();
			}
			String downloadPath = "";

			if ("".equals(outFileTxtField.getText())) {
				downloadPath = getDefaultDownload();
			} else {
				downloadPath = outFileTxtField.getText();
			}

			handleDownload(torrentName, downloadPath, selectFileIndex, torrentId, fileList);
			// Closing the dialog upon downloading
			handleCancel();
		});

		downloadCancelContent.getChildren().addAll(cancelBtn, downloadBtn);

		root.getChildren().add(downloadCancelContent);

		Scene scene = new Scene(root, 550, 500);
		scene.getStylesheets().add("File:CSS/style.css");

		stage.setScene(scene);
		stage.setResizable(false);
		stage.setOnCloseRequest(windowEvent -> {
			isOpen = false;
		});
		stage.show();

	}

	public static boolean isOpen() {
		return isOpen;
	}

	private static File openFileChooser() {
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Choose a downlaod location");

		dirChooser.setInitialDirectory(new File(getDefaultDownload()));

		return dirChooser.showDialog(stage);

	}

	private static String getDefaultDownload() {
		if (!System.getProperty("os.name").startsWith("Windows")) {
			return System.getProperty("user.home") + "/Downloads/";
		} else {
			return System.getProperty("user.home") + "/YIFY-Movies/";
		}
	}

	private static void handleCancel() {
		stage.hide();
		isOpen = false;

	}

	private static void handleDownload(String torrentName, String downloadPath, Integer selectFileIndex,
			String torrentId, ObservableList<MovieFile> fileList) {

		try {
			TorrentClient.start(StreamType.NONE, torrentName, downloadPath, selectFileIndex, torrentId, fileList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

//	protected class DialogResult {
//		private File outFile;
//		private Integer selctedDownloadIdx;
//
//		public DialogResult(File outFile, Integer selctedDownloadIdx) {
//			this.outFile = outFile;
//			this.selctedDownloadIdx = selctedDownloadIdx;
//		}
//
//		public File getOutFile() {
//			return outFile;
//		}
//
//		public Integer getSelctedDownloadIdx() {
//			return selctedDownloadIdx;
//		}
//
//	}
}
