package yify.view.ui;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
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
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.util.Duration;
import yify.model.movie.Movie;
import yify.model.movie.Torrent;
import yify.model.moviecatalog.MovieCatalog;
import yify.model.torrentclient.MovieFile;
import yify.model.torrentclient.StreamType;
import yify.model.torrentclient.TorrentClient;

public class MovieInfoPnl extends GridPane {
	/** The parts of the known YTS.mx URL split up */
	public static final String SCHEME = "https";
	public static final String HOST = "yts.torrentbay.to";
	public static final String PATH = "/api/v2/movie_details.json/";
	/** A constant for the id parameter as per the YTS.mx API */
	public static final String ID_PARAM = "movie_id=";
	/** A constant for the with_cast parameter as per the YTS.mx API */
	public static final String WITH_CAST_PARAM = "with_cast=";
	/** A constant for the with_images parameter as per the YTS.mx API */
	public static final String WITH_IMAGES_PARAM = "with_images=";
	private static final String DEFAULT_THUMBNAIL = "https://img.yts.mx/assets/images/actors/thumb/default_avatar.jpg";
	private static final Font ARIMO_BOLD40 = Font.loadFont("File:assets/fonts/arimo/Arimo-Bold.ttf", 40);
	private static final Font ARIMO_BOLD20 = Font.loadFont("File:assets/fonts/arimo/Arimo-Bold.ttf", 20);
	private static final Font ARIMO_BOLD18 = Font.loadFont("File:assets/fonts/arimo/Arimo-Bold.ttf", 18);
	private static final Font ARIMO_BOLD14 = Font.loadFont("File:assets/fonts/arimo/Arimo-Bold.ttf", 14);
	private static final Font ARIMO_ITALIC18 = Font.loadFont("File:assets/fonts/arimo/Arimo-Italic.ttf", 20);
	private static final Font ARIMO_REG14 = Font.loadFont("File:assets/fonts/arimo/Arimo-Regular.ttf", 14);
	private static final Font ARIMO_REG13 = Font.loadFont("File:assets/fonts/arimo/Arimo-Regular.ttf", 13);
	private static final Font ARIMO_REG16 = Font.loadFont("File:assets/fonts/arimo/Arimo-Regular.ttf", 16);

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
	private HBox titlePnl;
	private GridPane topContent;
	private GridPane bottomContent;
	private WebView webview;

	public MovieInfoPnl(Movie movie) {
		// Webview construction must be done on JavaFX thread.
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				webview = new WebView();
			}
		});

		try {
			System.out.println("Start movieDetails");
			getMovieDetails(movie);
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

		/********************* Initialize titlePnl ********************/
		initTitlePnl();
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
		// topContent.setGridLinesVisible(true);

		ImageView backgroundView = new ImageView(backgroundImg);
		backgroundView.fitWidthProperty().bind(titlePnl.widthProperty());
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

	private void initTitlePnl() {
		titlePnl = new HBox(950);
		titlePnl.setAlignment(Pos.CENTER_LEFT);
		titlePnl.setBackground(new Background(new BackgroundFill(Color.rgb(29, 29, 29, 1f), null, null)));
		titlePnl.setPadding(new Insets(10, 65, 10, 65));

		Image ytsLogo = new Image("File:assets/logo-YTS.png");
		ImageView imageView = new ImageView(ytsLogo);
		titlePnl.getChildren().add(imageView);

		ImageView taskViewerIcon = new ImageView(new Image("File:assets/taskManIcon.png"));

		Button taskViewerBtn = new Button("", taskViewerIcon);
		// hard coded insets for image size of icon
		taskViewerBtn.setBackground(
				new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, new Insets(9, 14, 9, 14))));
		taskViewerBtn.setFocusTraversable(false);
		taskViewerBtn.setTooltip(new Tooltip("View running tasks"));

		taskViewerBtn.setOnMouseEntered(moueseEvent -> {
			final Animation animation = new Transition() {

				{
					setCycleDuration(Duration.millis(180));
					setInterpolator(Interpolator.EASE_IN);
				}

				@Override
				protected void interpolate(double frac) {
					taskViewerBtn.setEffect(new DropShadow(5, 0, 0, Color.rgb(255, 255, 255, .40f * frac)));
				}
			};
			animation.play();

		});

		taskViewerBtn.setOnMouseExited(moueseEvent -> {
			final Animation animation = new Transition() {

				{
					setCycleDuration(Duration.millis(180));
					setInterpolator(Interpolator.EASE_IN);
				}

				@Override
				protected void interpolate(double frac) {
					taskViewerBtn.setEffect(new DropShadow(5, 0, 0, Color.rgb(255, 255, 255, .40f * (1 - frac))));
				}

			};
			animation.play();

		});

		taskViewerBtn.setOnMouseClicked(mouseEvent -> {
			TaskViewer.show();
		});

		titlePnl.getChildren().add(taskViewerBtn);

		titlePnl.setBorder(new Border(new BorderStroke(null, null, Color.rgb(47, 47, 47, 1f), null, null, null,
				BorderStrokeStyle.SOLID, null, CornerRadii.EMPTY, new BorderWidths(1), Insets.EMPTY)));
		this.add(titlePnl, 0, 0);

	}

	private void getMovieDetails(Movie movie) throws IOException, InterruptedException {
		HttpClient client = MovieCatalog.getClient();

		String queryString = ID_PARAM + movie.getId() + "&" + WITH_IMAGES_PARAM + true + "&" + WITH_CAST_PARAM + true;

		URI uri = null;
		try {
			uri = new URI(SCHEME, HOST, PATH, queryString, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		HttpResponse<String> response = null;

		try {
			// Create a request
			HttpRequest request = HttpRequest.newBuilder(uri).timeout(java.time.Duration.ofSeconds(10)).build();

			// Use the client to send the request
			response = client.send(request, BodyHandlers.ofString());
		} catch (Exception e) {
			// Any exceptions that occur here are most likely network exceptions so we
			// assume that the connection is bad.
			MovieCatalog.setConnectionOkay(false);
			BrowserPnl.loadMovies(false);
			return;
		}

		MovieCatalog.setConnectionOkay(true);

		// the response:
		String jsonString = "";
		if (response != null)
			jsonString = response.body();
		else
			// Should hopefully never happen.
			return;
		System.out.println(jsonString);
		System.out.println(uri.toString());

		JSONObject rawPage = (JSONObject) ((JSONObject) new JSONObject(jsonString).get("data")).get("movie");
		System.out.println("Start parse");
		parseRawPage(rawPage);
		System.out.println("End parse");
	}

	private void parseRawPage(JSONObject rawPage) {
		backgroundImg = new Image(rawPage.getString("background_image"), false);
		plotSummary = rawPage.getString("description_full");
		JSONArray rawTors = rawPage.getJSONArray("torrents");

		torrents = new Torrent[rawTors.length()];

		for (int i = 0; i < rawTors.length(); i++) {
			JSONObject rawTorrent = (JSONObject) rawTors.get(i);

			String url = rawTorrent.getString("url");
			String quality = rawTorrent.getString("quality");
			String type = rawTorrent.getString("type");
			int seeds = rawTorrent.getInt("seeds");
			int peers = rawTorrent.getInt("peers");
			String size = rawTorrent.getString("size");

			torrents[i] = new Torrent(url, quality, type, seeds, peers, size);

		}

		screenshotLinks = new String[3];
		for (int i = 0; i < 3; i++) {
			screenshotLinks[i] = rawPage.getString("medium_screenshot_image" + (i + 1));
		}

		ytTrailerCode = rawPage.getString("yt_trailer_code");

		cast = new LinkedHashMap<String, String>();

		JSONArray rawCast = null;
		try {
			rawCast = rawPage.getJSONArray("cast");
		} catch (JSONException e) {
			return;
		}
		for (int i = 0; i < rawCast.length(); i++) {
			JSONObject currentCast = rawCast.getJSONObject(i);
			String name = currentCast.getString("name");
			String characterName = currentCast.getString("character_name");

			String thumbnail;
			try {
				thumbnail = currentCast.getString("url_small_image");
			} catch (JSONException e) {
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
			Main.switchSceneContent(Main.getBrowserPnl());
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
		ImageView coverImg = new ImageView(movie.getThumbnail());
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
		streamBtn.setFont(ARIMO_BOLD18);
		GridPane.setMargin(streamBtn, new Insets(10, 0, 0, 40));
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

		// GridPane.setConstraints(streamBtn, 1, 1, 1, 2);
		topContent.add(streamBtn, 1, 2);

	}

	private void initMovieDetails(Movie movie) {
		VBox detailsContainer = new VBox(20);
		detailsContainer.setMaxWidth(450);

		// Movie title label
		Label movieTitle = new Label(movie.getTitle());
		System.out.println(movie.getTitle());
		movieTitle.setEffect(new DropShadow(2, 0, 2, Color.rgb(0, 0, 0, 0.75f)));
		movieTitle.setFont(ARIMO_BOLD40);
		movieTitle.setWrapText(true);
		movieTitle.setTextFill(Color.WHITE);
		movieTitle.setPadding(new Insets(0, 0, 20, 0));

		detailsContainer.getChildren().add(movieTitle);

		// Year and Genres label
		Label yearAndGenres = new Label();
		yearAndGenres.setEffect(new DropShadow(2, 0, 2, Color.rgb(0, 0, 0, 0.75f)));
		yearAndGenres.setFont(ARIMO_BOLD20);
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
		availableIn.setFont(ARIMO_ITALIC18);
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
			currBtn.setFont(ARIMO_REG13);
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
			rating.setFont(ARIMO_BOLD20);
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
		plotSumTitle.setFont(ARIMO_BOLD20);
		plotSumTitle.setTextFill(Color.WHITE);
		summaryContainer.getChildren().add(plotSumTitle);

		Label plotSummary = new Label(this.plotSummary);
		plotSummary.setFont(ARIMO_REG16);
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

		for (int i = 0; i < screenshotLinks.length; i++) {
			if (screenshotLinks[i] != null && !"".equals(screenshotLinks[i])) {
				System.out.println(screenshotLinks[i]);
				StackPane screenshotPane = new StackPane();
				ImageView screenshotImg = new ImageView(new Image(screenshotLinks[i], true));
				screenshotImg.setFitWidth(350);
				screenshotImg.setFitHeight(197);
				screenshotPane.getChildren().add(screenshotImg);

				if (i == 0 && playVidIcon != null) {
					screenshotPane.getChildren().add(playVidIcon);
				}

				Rectangle overlay = new Rectangle(350, 197);
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
						screenshotPane.getChildren().clear();
						webview.getEngine().load("https://youtube.com/embed/" + ytTrailerCode + "?autoplay=1");
						webview.setPrefSize(350, 197);
						screenshotPane.getChildren().add(webview);
					});
				}

				screenshots.getChildren().add(screenshotPane);

			}
		}

		GridPane.setHalignment(screenshots, HPos.CENTER);
		bottomContent.add(screenshots, 0, 0);
	}

	private void initCast() {
		VBox castBox = new VBox(10);
		castBox.setMaxWidth(400);
		castBox.setPadding(new Insets(0, 0, 70, 0));
		// castBox.setBackground(new Background(new BackgroundFill(Color.RED, null,
		// null)));

		Label topCast = new Label("Top Cast");
		topCast.setEffect(new DropShadow(2, 0, 2, Color.rgb(0, 0, 0, 0.75f)));
		topCast.setFont(ARIMO_BOLD20);
		topCast.setTextFill(Color.WHITE);

		castBox.getChildren().add(topCast);

		for (Map.Entry<String, String> entry : cast.entrySet()) {
			HBox actorBox = new HBox(10);
			actorBox.setAlignment(Pos.CENTER_LEFT);
			actorBox.setPadding(new Insets(0, 0, 10, 0));
			// actorBox.setBackground(new Background(new BackgroundFill(Color.rgb(50, 50, 50
			// * cnt), null, null)));
			// actorBox.setMaxWidth(200);
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
			actorNameLbl.setFont(ARIMO_BOLD14);
			actorNameLbl.setTextFill(Color.rgb(145, 145, 145));

			Label characterNameLbl = null;
			if (actor.length > 1) {
				characterNameLbl = new Label("as " + actor[1]);
				characterNameLbl.setPadding(new Insets(0, 0, 0, -5));
				characterNameLbl.setFont(ARIMO_REG14);
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

}
