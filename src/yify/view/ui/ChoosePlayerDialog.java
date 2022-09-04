package yify.view.ui;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import yify.model.torrentclient.MovieFile;
import yify.model.torrentclient.StreamType;
import yify.model.torrentclient.TorrentClient;
import yify.view.ui.util.SingleSelectionModelImpl;

final class ChoosePlayerDialog {
	private static final String VLC_NAME = "VLC";
	private static final String IINA_NAME = "IINA";
	private static final String MPLAYER_NAME = "MPlayer";
	private static final String MPV_NAME = "MPV";
	private static final String SMPLAYER_NAME = "SMPlayer";
	private static final Font ARIMO_BOLD24 = Font.loadFont("File:assets/fonts/arimo/Arimo-Bold.ttf", 24);
	private static Stage stage;
	private static GridPane main;
	private static GridPane topContent;
	private static VBox bottomContent;
	private static ComboBox<String> qualityCombo;
	private static RadioButton keepRadBtn;
	private static Button streamBtn;
	private static SingleSelectionModelImpl<ImageView> selectionModel;
	private static Map<Button, String> buttonsAndLinks;

	protected static void show(Map<Button, String> buttonsAndLinks) {
		Objects.requireNonNull(buttonsAndLinks);
		
		// Updating this field every time the dialog is show ensures the combo box and
		// the stream button have the correct options and links
		ChoosePlayerDialog.buttonsAndLinks = buttonsAndLinks;

		if (stage != null) {
			showStage();
			updateQualityCombo();
		} else {
			stage = new Stage();
			main = new GridPane();
			main.setAlignment(Pos.TOP_CENTER);
			main.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(25), Insets.EMPTY)));
			main.setBorder(new Border(new BorderStroke(Color.rgb(50, 50, 50), BorderStrokeStyle.SOLID,
					new CornerRadii(21), new BorderWidths(2))));
			main.setOpacity(0);

			initTopContent();
			initBottomContent(ChoosePlayerDialog.buttonsAndLinks);

			Scene scene = new Scene(main, 1000, 500);
			scene.setFill(Color.TRANSPARENT);
			scene.getStylesheets().add("File:CSS/chooseDialog.css");

			stage.setScene(scene);
			stage.initStyle(StageStyle.TRANSPARENT);
			stage.setAlwaysOnTop(true);
			showStage();

		}
	}

	private static void showStage() {
		stage.show();

		Timeline timeline = new Timeline();
		KeyFrame key = new KeyFrame(Duration.millis(200),
				new KeyValue(stage.getScene().getRoot().opacityProperty(), 1));
		timeline.getKeyFrames().add(key);
		timeline.play();
	}

	private static void hideStage() {
		Timeline timeline = new Timeline();
		KeyFrame key = new KeyFrame(Duration.millis(200),
				new KeyValue(stage.getScene().getRoot().opacityProperty(), 0));
		timeline.getKeyFrames().add(key);
		timeline.play();

		timeline.setOnFinished(actionEvent -> {
			stage.hide();
		});
	}

	private static void initTopContent() {
		topContent = new GridPane();
		topContent.setAlignment(Pos.TOP_CENTER);
		topContent.setHgap(50);
		topContent.setVgap(25);
		topContent.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, Color.TRANSPARENT, Color.rgb(70, 70, 70),
				Color.TRANSPARENT, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID,
				BorderStrokeStyle.NONE, CornerRadii.EMPTY, new BorderWidths(1), Insets.EMPTY)));
		topContent.setPadding(new Insets(0, 0, 40, 0));

		Label titleLbl = new Label("Choose a video player");
		titleLbl.setPadding(new Insets(40, 0, 25, 0));
		titleLbl.setFont(ARIMO_BOLD24);
		titleLbl.setTextFill(Color.rgb(70, 70, 70));

		topContent.add(titleLbl, 0, 0, 2, 1);

		DropShadow dropShadow = new DropShadow(5, 5, 5, Color.rgb(0, 0, 0, 0.40f));

		selectionModel = new SingleSelectionModelImpl<ImageView>(
				new ImageView[] { makeIcon("file:assets/iinaIcon.png", IINA_NAME, dropShadow, 0, 1),
						makeIcon("file:assets/VLC_Icon.png", "VLC", dropShadow, 1, 1),
						makeIcon("file:assets/mplayerIcon.png", MPLAYER_NAME, dropShadow, 2, 1),
						makeIcon("file:assets/mpvIcon.png", MPV_NAME, dropShadow, 3, 1),
						makeIcon("file:assets/smPlayerIcon.png", SMPLAYER_NAME, dropShadow, 4, 1) });

		main.add(topContent, 0, 0);

	}

	private static ImageView makeIcon(String fileName, String iconName, DropShadow ds, int col, int row) {
		ImageView icon = new ImageView(new Image(fileName));
		icon.setId(iconName);
		GridPane.setHalignment(icon, HPos.CENTER);
		icon.setEffect(ds);

		icon.setOnMouseEntered(mouseEvent -> {
			fadeEffect(mouseEvent, true);
		});
		icon.setOnMouseExited(mouseEvent -> {
			fadeEffect(mouseEvent, false);
		});

		icon.setOnMouseClicked(mouseEvent -> {
			if (streamBtn.isDisabled()) {
				streamBtn.setDisable(false);
			}
			// Reseting previously selected item before proceeding
			if (selectionModel.getSelected() != null)
				selectionModel.getSelected().setEffect(ds);

			selectionModel.select((ImageView) mouseEvent.getSource());

			final Animation animation = new Transition() {

				{
					setCycleDuration(Duration.millis(200));
					setInterpolator(Interpolator.EASE_BOTH);
				}

				@Override
				protected void interpolate(double frac) {
					((ImageView) mouseEvent.getSource())
							.setEffect(new DropShadow(
									BlurType.GAUSSIAN, Color.rgb((int) Math.ceil(106 * frac),
											(int) Math.ceil(192 * frac), (int) Math.ceil(69 * frac), 0.80f),
									30, .7f, 0, 0));
				}
			};
			animation.play();

		});

		topContent.add(icon, col, row);

		Label iconNameLbl = new Label(iconName);
		iconNameLbl.setFont(ARIMO_BOLD24);
		iconNameLbl.setTextFill(Color.rgb(70, 70, 70));
		GridPane.setHalignment(iconNameLbl, HPos.CENTER);

		topContent.add(iconNameLbl, col, row + 1);

		return icon;

	}

	private static void fadeEffect(MouseEvent mouseEvent, boolean fadeIn) {
		final Animation animation = new Transition() {

			{
				setCycleDuration(Duration.millis(200));
				setInterpolator(Interpolator.EASE_BOTH);
			}

			@Override
			protected void interpolate(double frac) {
				if (!selectionModel.isSelected((ImageView) mouseEvent.getSource())) {
					if (fadeIn) {
						float var = (float) ((5 * frac) + 5);
						((ImageView) mouseEvent.getSource())
								.setEffect(new DropShadow(var, var, var, Color.rgb(0, 0, 0, 0.40f)));
					} else {
						float var = (float) ((5 * (1 - frac)) + 5);
						((ImageView) mouseEvent.getSource())
								.setEffect(new DropShadow(var, var, var, Color.rgb(0, 0, 0, 0.40f)));
					}
				}
			}
		};
		animation.play();
	}

	private static void initBottomContent(Map<Button, String> buttonsAndLinks) {
		bottomContent = new VBox(15);
		bottomContent.setAlignment(Pos.TOP_CENTER);
		bottomContent.setPadding(new Insets(35, 0, 0, 0));

		HBox options = new HBox(10);
		options.setAlignment(Pos.TOP_LEFT);

		Label qualityLbl = new Label("Quality");
		qualityLbl.setFont(ARIMO_BOLD24);
		qualityLbl.setTextFill(Color.rgb(70, 70, 70));

		qualityCombo = new ComboBox<String>();
		qualityCombo
				.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
		qualityCombo.setBorder(new Border(new BorderStroke(Color.rgb(70, 70, 70), BorderStrokeStyle.SOLID,
				new CornerRadii(5), new BorderWidths(2))));
		qualityCombo.setFocusTraversable(false);

		updateQualityCombo();

		keepRadBtn = new RadioButton();

		Label keepLbl = new Label("Keep streamed movie");
		keepLbl.setFont(ARIMO_BOLD24);
		keepLbl.setTextFill(Color.rgb(70, 70, 70));
		keepLbl.setPadding(new Insets(0, 0, 0, 20));
		keepLbl.setGraphic(keepRadBtn);
		keepLbl.setContentDisplay(ContentDisplay.RIGHT);
		keepLbl.setOnMouseClicked(mouseEvent -> {
			keepRadBtn.requestFocus();
			keepRadBtn.setSelected(!keepRadBtn.isSelected());
		});

		options.getChildren().addAll(qualityLbl, qualityCombo, keepLbl);

		HBox streamCancelBox = new HBox(15);
		streamCancelBox.setAlignment(Pos.BOTTOM_RIGHT);
		streamCancelBox.setPadding(new Insets(30, 0, 0, 0));

		Button cancelBtn = new Button("Cancel");
		cancelBtn.setFont(new Font("Arial", 14));
		cancelBtn.setTextFill(Color.WHITE);
		cancelBtn.setPadding(new Insets(5, 20, 5, 20));
		cancelBtn.setEffect(new DropShadow(5, 3, 3, Color.rgb(0, 0, 0, 0.25f)));
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
			hideStage();
		});

		streamBtn = new Button("Stream");
		streamBtn.setFont(new Font("Arial", 14));
		streamBtn.setTextFill(Color.WHITE);
		streamBtn.setPadding(new Insets(5, 20, 5, 20));
		streamBtn.setEffect(new DropShadow(5, 3, 3, Color.rgb(0, 0, 0, 0.25f)));
		streamBtn.setBackground(
				new Background(new BackgroundFill(Color.rgb(38, 110, 205), new CornerRadii(5), Insets.EMPTY)));
		streamBtn.setOnMousePressed(mouseEvent -> {
			streamBtn.setBackground(
					new Background(new BackgroundFill(Color.rgb(61, 149, 231), new CornerRadii(5), Insets.EMPTY)));
		});
		streamBtn.setOnMouseReleased(mouseEvent -> {
			streamBtn.setBackground(
					new Background(new BackgroundFill(Color.rgb(38, 110, 205), new CornerRadii(5), Insets.EMPTY)));
		});
		streamBtn.setOnAction(actionEvent -> {
			handleStream();
		});

		// If no player is selected disable button
		if (selectionModel.getSelected() == null)
			streamBtn.setDisable(true);

		streamCancelBox.getChildren().addAll(cancelBtn, streamBtn);

		bottomContent.getChildren().addAll(options, streamCancelBox);

		main.add(bottomContent, 0, 1);
	}

	private static void updateQualityCombo() {
		Iterator<Button> iter = ChoosePlayerDialog.buttonsAndLinks.keySet().iterator();

		ObservableList<String> items = FXCollections.observableArrayList();
		while (iter.hasNext()) {
			items.add(iter.next().getText());
		}

		qualityCombo.setItems(items);
		qualityCombo.getSelectionModel().select(0);
	}

	private static void handleStream() {
		String url = "";
		Iterator<Button> iter = ChoosePlayerDialog.buttonsAndLinks.keySet().iterator();
		while (iter.hasNext()) {
			Button curr = iter.next();
			if (qualityCombo.getSelectionModel().getSelectedItem().equals(curr.getText())) {
				url = ChoosePlayerDialog.buttonsAndLinks.get(curr);
			}
		}

		ObservableList<MovieFile> fileList = TorrentClient.getFileList(url);
		String torrentName = fileList.get(0).getTorrentName();

		if (keepRadBtn.isSelected()) {
			hideStage();
			DownloadDialog.show(getStreamType(), torrentName, fileList, url);
		} else {
			try {
				hideStage();
				TorrentClient.start(getStreamType(), torrentName, null, null, url, fileList);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private static StreamType getStreamType() {
		String streamName = selectionModel.getSelected().getId();

		if (streamName.equals(VLC_NAME)) {
			return StreamType.VLC;
		} else if (streamName.equals(IINA_NAME)) {
			return StreamType.IINA;
		} else if (streamName.equals(MPLAYER_NAME)) {
			return StreamType.MPLAYER;
		} else if (streamName.equals(MPV_NAME)) {
			return StreamType.MPV;
		} else if (streamName.equals(SMPLAYER_NAME)) {
			return StreamType.SMPLAYER;
		} else {
			return StreamType.NONE;

		}
	}
}
