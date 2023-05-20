package yify.view.ui;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import yify.model.api.yts.YTS_API;
import yify.model.movie.Movie;
import yify.model.movie.Torrent;
import yify.model.torrentclient.MovieFile;
import yify.model.torrentclient.StreamType;
import yify.model.torrentclient.TorrentClient;
import yify.view.ui.util.Fonts;

public class MovieInfoPnl extends GridPane {
	
	private Image backgroundImg;
	private String plotSummary;
	private Torrent[] torrents;
	private String[] screenshotLinks;
	private String ytTrailerCode;
	/**
	 * A map containing the actor name and character name sperated by a ':' as the
	 * key, and a URL to their thumbnail as the value.
	 */
	private Map<String, String> cast;
	private Map<Button, String> buttonsAndLinks;
	private GridPane topContent;
	private GridPane bottomContent;
	private WebView webview;
	
	private static final String DEFAULT_THUMBNAIL = "https://img.yts.mx/assets/images/actors/thumb/default_avatar.jpg";

	public MovieInfoPnl(@SuppressWarnings("exports") Movie movie) {
		// Webview construction must be done on JavaFX thread.
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				webview = new WebView();
			}
		});

		try {
			System.out.println("Start movieDetails");
			JsonObject rawPage = YTS_API.instance().getMovieDetails(movie);
			
			if (rawPage != null) {
				parseRawPage(rawPage);
			}
			
			System.out.println("End movieDetails");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		this.setBackground(new Background(new BackgroundFill(Color.rgb(29, 29, 29, 1f), null, null)));
		this.setPrefHeight(Screen.getPrimary().getBounds().getHeight());
		// this.setGridLinesVisible(true);

		ColumnConstraints column = new ColumnConstraints();
		column.setPercentWidth(100);
		this.getColumnConstraints().add(column);

		/********************** Add background and overlay ******************/
		// Creating a GridPane to act as the overlay AND a container for the elements on
		// top of the background.
		topContent = new GridPane();
		topContent.setPrefHeight(580);

		// The overlay has a gradient color
		Stop[] stops = new Stop[] { new Stop(0, Color.rgb(29, 29, 29, 0.6f)), new Stop(1, Color.rgb(29, 29, 29, 1f)) };
		LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);

		topContent.setBackground(new Background(new BackgroundFill(gradient, null, null)));
		topContent.setPadding(new Insets(25, 0, 0, 45));

		// Waiting for background to load. Sometimes the background never loads so this
		// loop
		// goes on forever. To fix this I am adding a timeout so that if it doesn't load
		// within
		// two seconds then we break out of the loop.
		long start = System.currentTimeMillis();
		while (backgroundImg.getProgress() != 1.0) {
			if (System.currentTimeMillis() - start > 2000 || backgroundImg.isError()) {	
				System.out.println("Background loading time out.");
				break;
			}
		}
		ImageView backgroundView = new ImageView(backgroundImg);
		backgroundView.fitWidthProperty().bind(this.widthProperty());
		backgroundView.setFitHeight(580);

		// Image goes down before overlay
		this.add(backgroundView, 0, 1);
		this.add(topContent, 0, 1);

		/********************** Add back button *****************************/
		initBackBtn();
		/********************** Add cover image *****************************/
		initCoverImg(movie);
		/********************** Add streamBtn *******************************/
		initStreamBtn();
		/********************** Add cover image *****************************/
		initMovieDetails(movie);
		/********************** Add bottom content pane *********************/
		bottomContent = new GridPane();
		bottomContent.setAlignment(Pos.TOP_CENTER);
		/********************** Add screenshots *****************************/
		initScreenshots();
		/********************** Add cast if present *************************/
		if (!cast.isEmpty())
			initCast();

		this.add(bottomContent, 0, 2);

	}
	

	private void parseRawPage(JsonObject rawPage) {
		backgroundImg = new Image(rawPage.get("background_image").getAsString(), true);
		plotSummary = rawPage.get("description_full").getAsString();
		JsonArray rawTors = rawPage.get("torrents").getAsJsonArray();

		torrents = new Torrent[rawTors.size()];

		for (int i = 0; i < rawTors.size(); i++) {
			JsonObject rawTorrent = rawTors.get(i).getAsJsonObject();

			String url = rawTorrent.get("url").getAsString();
			String quality = rawTorrent.get("quality").getAsString();
			String type = rawTorrent.get("type").getAsString();
			int seeds = rawTorrent.get("seeds").getAsInt();
			int peers = rawTorrent.get("peers").getAsInt();
			String size = rawTorrent.get("size").getAsString();

			torrents[i] = new Torrent(url, quality, type, seeds, peers, size);

		}

		screenshotLinks = new String[3];

		// Switching to a more reliable source for screenshots
		for (int i = 0; i < 3; i++) {
			screenshotLinks[i] = rawPage.get("medium_screenshot_image" + (i + 1)).getAsString().replace("yts.mx",
					"img.yts.mx");
		}

		ytTrailerCode = rawPage.get("yt_trailer_code").getAsString();

		cast = new LinkedHashMap<String, String>();

		JsonArray rawCast = rawPage.getAsJsonArray("cast");
		if (rawCast == null) {
			return;
		}

		for (int i = 0; i < rawCast.size(); i++) {
			JsonObject currentCast = rawCast.get(i).getAsJsonObject();
			String name = currentCast.get("name").getAsString();
			String characterName = currentCast.get("character_name").getAsString();

			JsonElement thumbnailElement = currentCast.get("url_small_image");

			String thumbnail = null;
			if (thumbnailElement != null)
				thumbnail = thumbnailElement.getAsString();

			if (thumbnail == null) {
				thumbnail = DEFAULT_THUMBNAIL;
			}

			cast.put(name + ":" + characterName, thumbnail);
		}

	}

	private void initBackBtn() {

		ImageView backBtn = new ImageView(new Image("file:assets/back_arrow.png"));
		HBox backBtnBox = new HBox(backBtn);
		backBtnBox.setMaxSize(50, 50);
		GridPane.setValignment(backBtnBox, VPos.TOP);

		backBtnBox.setOnMouseClicked(mouseEvent -> {
			webview.getEngine().load(null);
			App.switchSceneContent(App.getBrowserPnl());
		});

		backBtnBox.setOnMouseEntered(mouseEvent -> {
			final Animation animation = new Transition() {

				{
					setCycleDuration(javafx.util.Duration.millis(300));
					setInterpolator(Interpolator.EASE_BOTH);
				}

				@Override
				protected void interpolate(double frac) {
					double rVariable = (255 - 106) * frac;
					int r = (int) Math.ceil(255 - rVariable);
					double gVariable = (255 - 192) * frac;
					int g = (int) Math.ceil(255 - gVariable);
					double bVariable = (255 - 69) * frac;
					int b = (int) Math.ceil(255 - bVariable);

					Color vColor = Color.rgb(r, g, b);

					Lighting lighting = new Lighting(new Light.Distant(45, 90, vColor));
					ColorAdjust bright = new ColorAdjust(0, 1, 1, 1);
					lighting.setContentInput(bright);
					lighting.setSurfaceScale(0.0);

					backBtn.setEffect(lighting);

				}
			};
			animation.play();
		});

		backBtnBox.setOnMouseExited(mouseEvent -> {
			final Animation animation = new Transition() {

				{
					setCycleDuration(javafx.util.Duration.millis(300));
					setInterpolator(Interpolator.EASE_BOTH);
				}

				@Override
				protected void interpolate(double frac) {
					double rVariable = (255 - 106) * frac;
					int r = (int) Math.ceil(106 + rVariable);
					double gVariable = (255 - 192) * frac;
					int g = (int) Math.ceil(192 + gVariable);
					double bVariable = (255 - 69) * frac;
					int b = (int) Math.ceil(69 + bVariable);

					Color vColor = Color.rgb(r, g, b);

					Lighting lighting = new Lighting(new Light.Distant(45, 90, vColor));
					ColorAdjust bright = new ColorAdjust(0, 1, 1, 1);
					lighting.setContentInput(bright);
					lighting.setSurfaceScale(0.0);

					backBtn.setEffect(lighting);

				}

			};
			animation.play();
		});

		topContent.add(backBtnBox, 0, 0);

	}

	private void initCoverImg(Movie movie) {
		ImageView coverImg = null;
	
		if (movie.getThumbnail() == null) {
			coverImg = new ImageView(movie.getThumbnailUrl());
		} else {
			coverImg = new ImageView(movie.getThumbnail());
		}
		
		coverImg.setFitWidth(263);
		coverImg.setFitHeight(390);

		VBox parentContainer = new VBox(10);
		parentContainer.setPadding(new Insets(0, 85, 0, 35));

		VBox coverImgContainer = new VBox(coverImg);

		coverImgContainer.setBorder(new Border(
				new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(5))));
		parentContainer.getChildren().add(coverImgContainer);

		GridPane.setConstraints(parentContainer, 1, 0, 1, 2);
		topContent.getChildren().add(parentContainer);

	}

	private void initStreamBtn() {
		Button streamBtn = new Button("Watch Now");
		streamBtn.setBackground(
				new Background(new BackgroundFill(Color.rgb(106, 192, 69, 1f), new CornerRadii(3), null)));
		streamBtn.setPrefSize(263, 40);
		streamBtn.setTextFill(Color.WHITE);
		streamBtn.setFont(Fonts.ARIMO_BOLD18);
		// GridPane.setMargin(streamBtn, new Insets(10, 0, 0, 40));
		streamBtn.setOnMouseEntered(mouseEvent -> {
			final Animation animation = new Transition() {

				{
					setCycleDuration(Duration.millis(300));
					setInterpolator(Interpolator.EASE_BOTH);
				}

				@Override
				protected void interpolate(double frac) {
					double rVariable = (106 - 93) * frac;
					int r = (int) Math.ceil(106 - rVariable);
					double gVariable = (192 - 169) * frac;
					int g = (int) Math.ceil(192 - gVariable);
					double bVariable = (69 - 60) * frac;
					int b = (int) Math.ceil(69 - bVariable);

					Color vColor = Color.rgb(r, g, b);
					streamBtn.setBackground(new Background(new BackgroundFill(vColor, new CornerRadii(3), null)));
				}
			};
			animation.play();
		});

		streamBtn.setOnMouseExited(mouseEvent -> {
			final Animation animation = new Transition() {

				{
					setCycleDuration(Duration.millis(300));
					setInterpolator(Interpolator.EASE_BOTH);
				}

				@Override
				protected void interpolate(double frac) {
					double rVariable = (106 - 93) * frac;
					int r = (int) Math.ceil(93 + rVariable);
					double gVariable = (192 - 169) * frac;
					int g = (int) Math.ceil(169 + gVariable);
					double bVariable = (69 - 60) * frac;
					int b = (int) Math.ceil(60 + bVariable);
					Color vColor = Color.rgb(r, g, b);
					streamBtn.setBackground(new Background(new BackgroundFill(vColor, new CornerRadii(3), null)));
				}

			};
			animation.play();
		});

		streamBtn.setOnMouseClicked(mouseEvent -> {
			ChoosePlayerDialog.show(buttonsAndLinks);
		});

		// This is janky but we are adding to the parent container of the coverImg. As
		// long as nothing is added after that node this should work.
		((VBox) topContent.getChildren().get(topContent.getChildren().size() - 1)).getChildren().add(streamBtn);

	}

	private void initMovieDetails(Movie movie) {
		VBox detailsContainer = new VBox(20);
		detailsContainer.setMaxWidth(450);

		// Movie title label
		Label movieTitle = new Label(movie.getTitle());
		System.out.println(movie.getTitle());
		movieTitle.setEffect(new DropShadow(2, 0, 2, Color.rgb(0, 0, 0, 0.75f)));
		movieTitle.setFont(Fonts.ARIMO_BOLD40);
		movieTitle.setWrapText(true);
		movieTitle.setTextFill(Color.WHITE);
		movieTitle.setPadding(new Insets(0, 0, 20, 0));

		detailsContainer.getChildren().add(movieTitle);

		// Year and Genres label
		Label yearAndGenres = new Label();
		yearAndGenres.setEffect(new DropShadow(2, 0, 2, Color.rgb(0, 0, 0, 0.75f)));
		yearAndGenres.setFont(Fonts.ARIMO_BOLD20);
		yearAndGenres.setWrapText(true);
		yearAndGenres.setTextFill(Color.WHITE);
		yearAndGenres.setPadding(new Insets(0, 0, 15, 0));

		String yearAndGenresStr = movie.getYear() + "\n";

		if (movie.getGenres() != null) {
			String[] genres = movie.getGenres();
			for (int i = 0; i < genres.length; i++) {
				yearAndGenresStr += genres[i];

				if (i != genres.length - 1) {
					yearAndGenresStr += " / ";
				}
			}
		}
		yearAndGenres.setText(yearAndGenresStr);

		detailsContainer.getChildren().add(yearAndGenres);

		// Download Buttons
		FlowPane downloadBtnsBox = new FlowPane();
		downloadBtnsBox.setHgap(5);
		downloadBtnsBox.setVgap(5);

		Label availableIn = new Label("Available in: ");
		availableIn.setFont(Fonts.ARIMO_ITALIC18);
		availableIn.setTextFill(Color.WHITE);
		downloadBtnsBox.getChildren().add(availableIn);

		// Initialize Map storing each button and its respective torrent link
		buttonsAndLinks = new LinkedHashMap<Button, String>();

		for (int i = 0; i < torrents.length; i++) {
			Torrent currTorrent = torrents[i];

			ImageView downloadIcon = new ImageView(new Image("file:assets/download_icon.png", false));
			downloadIcon.setFitWidth(12);
			downloadIcon.setFitHeight(12);

			Button currBtn = new Button(torrents[i].getQuality() + "." + currTorrent.getType(), downloadIcon);

			currBtn.setBackground(
					new Background(new BackgroundFill(Color.rgb(27, 27, 35, 0.4f), new CornerRadii(5), null)));
			currBtn.setBorder(new Border(new BorderStroke(Color.rgb(255, 255, 255, 0.16f), BorderStrokeStyle.SOLID,
					new CornerRadii(5), new BorderWidths(1))));
			currBtn.setTextFill(Color.WHITE);
			currBtn.setFont(Fonts.ARIMO_REG13);
			currBtn.setGraphicTextGap(5);
			buttonsAndLinks.put(currBtn, currTorrent.getUrl());

			// Button functionality
			currBtn.setOnMouseClicked(mouseEvent -> {
				beginDownload(mouseEvent, movie);
			});

			// Button hover animation
			currBtn.setOnMouseEntered(mouseEvent -> {
				final Animation animation = new Transition() {

					{
						setCycleDuration(javafx.util.Duration.millis(300));
						setInterpolator(Interpolator.EASE_BOTH);
					}

					@Override
					protected void interpolate(double frac) {
						double rVariable = (255 - 190) * frac;
						int r = (int) Math.ceil(255 - rVariable);
						double gVariable = (255 - 190) * frac;
						int g = (int) Math.ceil(255 - gVariable);
						double bVariable = (255 - 190) * frac;
						int b = (int) Math.ceil(255 - bVariable);

						Color vColor = Color.rgb(r, g, b);

						currBtn.setTextFill(vColor);

						Lighting lighting = new Lighting(new Light.Distant(45, 90, vColor));
						ColorAdjust bright = new ColorAdjust(0, 1, 1, 1);
						lighting.setContentInput(bright);
						lighting.setSurfaceScale(0.0);

						downloadIcon.setEffect(lighting);

					}
				};
				animation.play();
			});

			currBtn.setOnMouseExited(mouseEvent -> {
				final Animation animation = new Transition() {

					{
						setCycleDuration(javafx.util.Duration.millis(300));
						setInterpolator(Interpolator.EASE_BOTH);
					}

					@Override
					protected void interpolate(double frac) {
						double rVariable = (255 - 190) * frac;
						int r = (int) Math.ceil(190 + rVariable);
						double gVariable = (255 - 190) * frac;
						int g = (int) Math.ceil(190 + gVariable);
						double bVariable = (255 - 190) * frac;
						int b = (int) Math.ceil(190 + bVariable);

						Color vColor = Color.rgb(r, g, b);

						currBtn.setTextFill(vColor);

						Lighting lighting = new Lighting(new Light.Distant(45, 90, vColor));
						ColorAdjust bright = new ColorAdjust(0, 1, 1, 1);
						lighting.setContentInput(bright);
						lighting.setSurfaceScale(0.0);

						downloadIcon.setEffect(lighting);

					}
				};
				animation.play();
			});

			downloadBtnsBox.getChildren().add(currBtn);
		}

		detailsContainer.getChildren().add(downloadBtnsBox);

		// Rating Icons and label
		if (movie.getRating() != 0) {

			HBox ratingBox = new HBox(15);
			ImageView imdbIcon = new ImageView(new Image("file:assets/imdb-logo.png"));
			ratingBox.getChildren().add(imdbIcon);

			Label rating = new Label(Float.toString(movie.getRating()));
			rating.setFont(Fonts.ARIMO_BOLD20);
			rating.setTextFill(Color.WHITE);
			ratingBox.getChildren().add(rating);

			ImageView starIcon = new ImageView(new Image("file:assets/star.png"));
			starIcon.setFitHeight(20);
			starIcon.setFitWidth(20);
			ratingBox.getChildren().add(starIcon);

			detailsContainer.getChildren().add(ratingBox);

		}

		topContent.add(detailsContainer, 2, 0);

		VBox summaryContainer = new VBox(8);
		summaryContainer.setMaxWidth(650);
		summaryContainer.setPadding(new Insets(15, 0, 0, 0));

		Label plotSumTitle = new Label("Plot summary");
		plotSumTitle.setEffect(new DropShadow(2, 0, 2, Color.rgb(0, 0, 0, 0.75f)));
		plotSumTitle.setFont(Fonts.ARIMO_BOLD20);
		plotSumTitle.setTextFill(Color.WHITE);
		summaryContainer.getChildren().add(plotSumTitle);

		Label plotSummary = new Label(this.plotSummary);
		plotSummary.setFont(Fonts.ARIMO_REG16);
		plotSummary.setTextFill(Color.rgb(145, 145, 145, 1f));
		plotSummary.setWrapText(true);
		plotSummary.setMaxHeight(160);

		summaryContainer.getChildren().add(plotSummary);

		GridPane.setConstraints(summaryContainer, 2, 1, 2, 1);
		topContent.getChildren().add(summaryContainer);
	}

	private void beginDownload(MouseEvent mouseEvent, Movie movie) {
		Button btn = (Button) mouseEvent.getSource();
		String url = buttonsAndLinks.get(btn);

		System.out.println("Started getFileList()");
		ObservableList<MovieFile> fileList = TorrentClient.getFileList(url);
		System.out.println("Finished getFileList()");

		String torrentName = fileList.get(0).getTorrentName();

		System.out.println("Starting show dialog");
		DownloadDialog.show(StreamType.NONE, torrentName, fileList, url);
		System.out.println("End show dialog");

	}

	private void initScreenshots() {
		HBox screenshots = new HBox(5);
		VBox.setMargin(screenshots, new Insets(0, 110, 0, 0));
		screenshots.setPadding(new Insets(0, 0, 70, 0));
		screenshots.setAlignment(Pos.TOP_CENTER);

		System.out.println(ytTrailerCode);

		ImageView playVidIcon = null;
		if (!"".equals(ytTrailerCode)) {
			playVidIcon = new ImageView(new Image("file:assets/playBtnIcon.png", true));
		}

		// This array holds the images for the popup screenshots. Declared here so that
		// it can be used with the onMouseClick lambda expression.
		ImageView[] popupScreenshotsArr = new ImageView[3];

		ImageView[] screenshotsArr = new ImageView[3];

		for (int i = 0; i < screenshotLinks.length; i++) {
			if (screenshotLinks[i] != null && !"".equals(screenshotLinks[i])) {
				System.out.println(screenshotLinks[i]);
				StackPane screenshotPane = new StackPane();
				screenshotsArr[i] = new ImageView(new Image(screenshotLinks[i], true));
				ImageView screenshotImg = screenshotsArr[i];

				screenshotPane.getChildren().add(screenshotImg);

				if (i == 0 && playVidIcon != null) {
					screenshotPane.getChildren().add(playVidIcon);
				}

				Rectangle overlay = new Rectangle();
				overlay.widthProperty().bind(screenshotImg.getImage().widthProperty());
				overlay.heightProperty().bind(screenshotImg.getImage().heightProperty());

				overlay.setFill(Color.rgb(29, 29, 29, 0.0f));
				screenshotPane.getChildren().add(overlay);

				screenshotPane.setOnMouseEntered(mouseEvent -> {
					final Animation animation = new Transition() {

						{
							setCycleDuration(Duration.millis(180));
							setInterpolator(Interpolator.EASE_IN);
						}

						@Override
						protected void interpolate(double frac) {
							overlay.setFill(Color.rgb(29, 29, 29, 0.60f * frac));
						}
					};
					animation.play();
				});

				screenshotPane.setOnMouseExited(mouseEvent -> {
					final Animation animation = new Transition() {

						{
							setCycleDuration(Duration.millis(180));
							setInterpolator(Interpolator.EASE_IN);
						}

						@Override
						protected void interpolate(double frac) {
							overlay.setFill(Color.rgb(29, 29, 29, 0.60f * (1 - frac)));
						}

					};
					animation.play();
				});

				if (i == 0 && playVidIcon != null) {
					overlay.setOnMouseClicked(mouseEvent -> {

						// Disabling controls because of semi-transparent overlay that shows up on
						// hover.
						webview.getEngine().load("https://youtube.com/embed/" + ytTrailerCode
								+ "?autoplay=1&fs=0&playsinline=1&controls=0&rel=0");
						PopupStage webviewStage = new PopupStage(webview, this.getScene());
						webviewStage.showStage();
					});
				} else {
					final Integer currentIdx = Integer.valueOf(i);
					overlay.setOnMouseClicked(mouseEvent -> {
						new PopupStage(popupScreenshotsArr, currentIdx, getScene()).showStage();
					});
				}

				screenshots.getChildren().add(screenshotPane);

			}
		}

		// Waiting for screenshots to load.
		while (screenshotsArr[0].getImage().getProgress() != 1.0 || screenshotsArr[1].getImage().getProgress() != 1.0
				|| screenshotsArr[2].getImage().getProgress() != 1.0) {
			Thread.onSpinWait();
		}

		// We don't need the big screenshots urgently so load them in the background.
		for (int j = 0; j < screenshotLinks.length; j++) {
			popupScreenshotsArr[j] = new ImageView(new Image(screenshotLinks[j].replace("medium", "large"), true));
		}

		GridPane.setHalignment(screenshots, HPos.CENTER);
		bottomContent.add(screenshots, 0, 0);
	}

	private void initCast() {
		VBox castBox = new VBox(10);
		castBox.setMaxWidth(400);
		castBox.setPadding(new Insets(0, 0, 70, 0));

		Label topCast = new Label("Top Cast");
		topCast.setEffect(new DropShadow(2, 0, 2, Color.rgb(0, 0, 0, 0.75f)));
		topCast.setFont(Fonts.ARIMO_BOLD20);
		topCast.setTextFill(Color.WHITE);

		castBox.getChildren().add(topCast);

		for (Map.Entry<String, String> entry : cast.entrySet()) {
			HBox actorBox = new HBox(10);
			actorBox.setAlignment(Pos.CENTER_LEFT);
			actorBox.setPadding(new Insets(0, 0, 10, 0));
			actorBox.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, Color.TRANSPARENT, Color.rgb(47, 47, 47),
					Color.TRANSPARENT, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID,
					BorderStrokeStyle.NONE, CornerRadii.EMPTY, new BorderWidths(1), Insets.EMPTY)));

			String[] actor = entry.getKey().split(":");
			String url = entry.getValue();

			ImageView thumbnail = new ImageView(new Image(url, true));
			thumbnail.setFitWidth(40);
			thumbnail.setFitHeight(40);
			Circle clip = new Circle(20, 20, 20);

			thumbnail.setClip(clip);

			Label actorNameLbl = new Label(actor[0]);
			actorNameLbl.setFont(Fonts.ARIMO_BOLD14);
			actorNameLbl.setTextFill(Color.rgb(145, 145, 145));

			Label characterNameLbl = null;
			if (actor.length > 1) {
				characterNameLbl = new Label("as " + actor[1]);
				characterNameLbl.setPadding(new Insets(0, 0, 0, -5));
				characterNameLbl.setFont(Fonts.ARIMO_REG14);
				characterNameLbl.setTextFill(Color.WHITE);
			}

			actorBox.getChildren().addAll(thumbnail, actorNameLbl);

			if (characterNameLbl != null)
				actorBox.getChildren().add(characterNameLbl);

			castBox.getChildren().add(actorBox);
		}

		GridPane.setHalignment(castBox, HPos.RIGHT);

		bottomContent.add(castBox, 0, 1);
	}

	private class PopupStage extends Stage {
		private int pos;
		private ImageView[] screenshots;
		private Scene parentScene;

		private PopupStage(Scene parentScene) {
			this.parentScene = parentScene;
			VBox parentContainer = new VBox(10);
			Scene scene = new Scene(parentContainer);
			scene.setFill(Color.TRANSPARENT);
			scene.getStylesheets().add("File:CSS/transparentStage.css");

			this.initModality(Modality.WINDOW_MODAL);
			this.initOwner(parentScene.getWindow());
			this.setScene(scene);
			this.initStyle(StageStyle.TRANSPARENT);
		}

		public PopupStage(WebView webview, Scene parentScene) {
			this(parentScene);

			// Setting width and height to be 70 percent of the parent stage's width and
			// height
			webview.prefWidthProperty().bind(parentScene.getWindow().widthProperty().multiply(0.70));
			webview.prefHeightProperty().bind(parentScene.getWindow().heightProperty().multiply(0.70));
			initNavBtns(true);
			((VBox) this.getScene().getRoot()).getChildren().add(webview);
		}

		public PopupStage(ImageView[] screenshots, int clickedIdx, Scene parentScene) {
			this(parentScene);
			this.pos = clickedIdx;
			this.screenshots = screenshots;

			for (int i = 0; i < screenshots.length; i++) {
				screenshots[i].fitWidthProperty().bind(screenshots[i].getImage().widthProperty().divide(1.5));
				screenshots[i].fitHeightProperty().bind(screenshots[i].getImage().heightProperty().divide(1.5));
			}

			initNavBtns(false);

			((VBox) this.getScene().getRoot()).getChildren().add(screenshots[clickedIdx]);

		}

		private void showStage() {
			// Handling darkening the parent stage
			final Animation animation = new Transition() {
				ColorAdjust adjust = new ColorAdjust(0, 0, 0, 0);
				{

					setCycleDuration(Duration.millis(180));
					setInterpolator(Interpolator.EASE_IN);
				}

				@Override
				protected void interpolate(double frac) {
					adjust.setBrightness(-(frac * 0.80));
					adjust.setContrast(-(frac * 0.40));
					parentScene.getRoot().setEffect(adjust);
				}
			};
			animation.play();

			// Handling showing the PopupStage
			this.setOpacity(0);
			this.show();

			Timeline timeline = new Timeline();
			KeyFrame key = new KeyFrame(Duration.millis(100), new KeyValue(this.opacityProperty(), 1));
			timeline.getKeyFrames().add(key);
			timeline.play();
		}

		private void hideStage() {
			// remove darkening effect
			parentScene.getRoot().setEffect(null);

			Timeline timeline = new Timeline();
			KeyFrame key = new KeyFrame(Duration.millis(200),
					new KeyValue(this.getScene().getRoot().opacityProperty(), 0));
			timeline.getKeyFrames().add(key);
			timeline.play();

			timeline.setOnFinished(actionEvent -> {
				if (webview != null) {
					webview.getEngine().load(null);
				}
				this.hide();
			});
		}

		private void initNavBtns(boolean limited) {
			HBox navBtnsBox = new HBox(10);
			navBtnsBox.setAlignment(Pos.TOP_RIGHT);

			// Load in navBtns as images
			if (!limited) {
				ImageView leftArrow = new ImageView("file:assets/leftArrow.png");
				leftArrow.setPickOnBounds(true);
				leftArrow.setOnMouseClicked(mouseEvent -> {
					left();
				});
				leftArrow.setOnMouseEntered(mouseEvent -> {
					applyBtnEffect(leftArrow);
				});
				leftArrow.setOnMouseExited(mouseEvent -> {
					leftArrow.setEffect(null);
				});

				ImageView rightArrow = new ImageView("file:assets/rightArrow.png");
				rightArrow.setPickOnBounds(true);
				rightArrow.setOnMouseClicked(mouseEvent -> {
					right();
				});
				rightArrow.setOnMouseEntered(mouseEvent -> {
					applyBtnEffect(rightArrow);
				});
				rightArrow.setOnMouseExited(mouseEvent -> {
					rightArrow.setEffect(null);
				});
				navBtnsBox.getChildren().addAll(leftArrow, rightArrow);

			}

			ImageView close = new ImageView("file:assets/close.png");
			close.setPickOnBounds(true);
			close.setOnMouseClicked(mouseEvent -> {
				hideStage();
			});
			close.setOnMouseEntered(mouseEvent -> {
				applyBtnEffect(close);
			});
			close.setOnMouseExited(mouseEvent -> {
				close.setEffect(null);
			});

			navBtnsBox.getChildren().add(close);
			((VBox) this.getScene().getRoot()).getChildren().add(navBtnsBox);

		}

		private void left() {
			if (pos == 0) {
				this.pos = screenshots.length - 1;
			} else {
				pos--;
			}

			((VBox) this.getScene().getRoot()).getChildren().set(1, screenshots[pos]);
		}

		private void right() {
			if (pos == screenshots.length - 1) {
				this.pos = 0;
			} else {
				pos++;
			}

			((VBox) this.getScene().getRoot()).getChildren().set(1, screenshots[pos]);
		}

		private void applyBtnEffect(ImageView btn) {
			Lighting lighting = new Lighting(new Light.Distant(45, 90, Color.WHITE));
			ColorAdjust bright = new ColorAdjust(0, 1, 1, 1);
			lighting.setContentInput(bright);
			lighting.setSurfaceScale(0.0);

			btn.setEffect(lighting);
		}
	}

}
