package yify.view.ui;

import java.io.IOException;
import java.util.ArrayList;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import yify.model.movie.Movie;
import yify.model.moviecatalog.MovieCatalog;
import yify.model.moviecatalog.searchquery.Genre;
import yify.model.moviecatalog.searchquery.Quality;
import yify.model.moviecatalog.searchquery.SearchQuery;
import yify.model.moviecatalog.searchquery.SortBy;
import yify.view.ui.util.BackgroundWorker;

public class BrowserPnl extends VBox {
	private static MovieCatalog instance;
	private HBox titlePnl;
	private GridPane searchPnl;
	private static VBox moviePnl;
	private Button searchBtn;
	private TextField searchTermTxt;
	private ComboBox<String> qualityCombo;
	private ComboBox<String> genreCombo;
	private ComboBox<String> ratingCombo;
	private ComboBox<String> sortByCombo;
	private Label moviePnlTitle;
	private static HBox navBtnsBox;
	private static final Font ARIAL_REG16 = new Font("Arial Regular", 16);
	private static final Font ARIMO_BOLD14 = Font.loadFont("File:assets/fonts/arimo/Arimo-Bold.ttf", 14);
	private static final Font ARIMO_BOLD12 = Font.loadFont("File:assets/fonts/arimo/Arimo-Bold.ttf", 12);
	private static final Font ARIMO_BOLD22 = Font.loadFont("File:assets/fonts/arimo/Arimo-Bold.ttf", 22);
	private static final Font ARIMO_REG22 = Font.loadFont("File:assets/fonts/arimo/Arimo-Regular.ttf", 22);
	private static final Font ARIMO_REG12 = Font.loadFont("File:assets/fonts/arimo/Arimo-Regular.ttf", 12);

	public BrowserPnl() {
		this.setBackground(new Background(new BackgroundFill(Color.rgb(29, 29, 29, 1f), null, null)));

//		ColumnConstraints column = new ColumnConstraints();
//		column.setPercentWidth(100);
//		this.getColumnConstraints().add(column);

		/********************* Initialize titlePnl START ********************/
		titlePnl = new HBox(950);
		titlePnl.setAlignment(Pos.CENTER_LEFT);
		titlePnl.setBackground(new Background(new BackgroundFill(Color.rgb(29, 29, 29, 1f), null, null)));
//		titlePnl.setPrefSize(1200, 60);
		titlePnl.setPadding(new Insets(10, 65, 10, 65));

		Image ytsLogo = new Image("File:assets/logo-YTS.png");
		ImageView imageView = new ImageView(ytsLogo);
		imageView.setFitWidth(127);
		imageView.setFitHeight(40);
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
		this.getChildren().add(titlePnl);
		/********************** Initialize titlePnl END *********************/

		/********************** Initialize searchPnl START ******************/
		searchPnl = new GridPane();
		searchPnl.setBackground(new Background(new BackgroundFill(Color.rgb(23, 23, 23, 1f), null, null)));
		searchPnl.setMinSize(950, 266);
		searchPnl.setPadding(new Insets(50, 160, 0, 160));

		initSearchBox();
		initQualityCombo();
		initGenreCombo();
		initRatingCombo();
		initSortByCombo();
		initSearchBtn();

		this.getChildren().add(searchPnl);
		/*********************** Initialize searchPnl END *******************/

		/*********************** Initialize moviePnl START ******************/
		moviePnl = new VBox(10);
		// moviePnl.setPrefWidth(1200);
		moviePnl.setAlignment(Pos.TOP_CENTER);

		moviePnlTitle = new Label("YIFY Movies");
		moviePnlTitle.setFont(ARIMO_REG22);
		moviePnlTitle.setTextFill(Color.rgb(106, 192, 69, 1f));
		moviePnlTitle.setPadding(new Insets(10, 0, 0, 0));
		moviePnl.getChildren().add(moviePnlTitle);

		initNavBtns();

		this.getChildren().add(moviePnl);

		/*********************** Initialize moviePnl END ********************/

		/*********************** Initialize MovieCatalog instance START *****/
		try {
			instance = MovieCatalog.instance();
			instance.makeRequest(SearchQuery.getDefaultSearchQuery());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		/*********************** Initialize MovieCatalog instance END *******/

	}

	private void initSearchBox() {
		Text searchTerm = new Text("Search Term: \n");
		searchTerm.setFont(ARIMO_BOLD22);
		searchTerm.setFill(Color.rgb(90, 90, 90));
		searchTerm.setLineSpacing(-10);

		searchPnl.add(searchTerm, 0, 0);

		searchTermTxt = new TextField();
		searchTermTxt.setBackground(new Background(
				new BackgroundFill(Color.rgb(40, 40, 40, 1f), new CornerRadii(3), new Insets(0, 25, 0, 0))));
		searchTermTxt.setFont(ARIAL_REG16);
		searchTermTxt.setStyle("-fx-text-inner-color: #a2a2a2;");
		searchTermTxt.setPrefSize(880, 40);
		searchTermTxt.setMaxSize(880, 40);
		searchTermTxt.setFocusTraversable(false);
		searchTermTxt.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode().equals(KeyCode.ENTER)) {
				submitSearch();
			}
		});
		GridPane.setConstraints(searchTermTxt, 0, 1, 5, 1);
		searchPnl.getChildren().add(searchTermTxt);

	}

	private void initQualityCombo() {
		VBox qualityBox = new VBox(15);
		qualityBox.setPadding(new Insets(20, 20, 0, 0));

		Text quality = new Text("Quality: ");
		quality.setFont(ARIMO_BOLD14);
		quality.setFill(Color.rgb(90, 90, 90));
		qualityBox.getChildren().add(quality);

		qualityCombo = new ComboBox<String>();
		qualityCombo.setPrefSize(130, 40);
		qualityCombo.setItems(FXCollections.observableList(Quality.getOptions()));
		qualityCombo.getSelectionModel().selectFirst();
		qualityCombo.setFocusTraversable(false);
		qualityBox.getChildren().add(qualityCombo);

		searchPnl.add(qualityBox, 0, 2);
	}

	private void initGenreCombo() {
		VBox genreBox = new VBox(15);
		genreBox.setPadding(new Insets(20, 20, 0, 0));

		Text genre = new Text("Genre: ");
		genre.setFont(ARIMO_BOLD14);
		genre.setFill(Color.rgb(90, 90, 90));
		genreBox.getChildren().add(genre);

		genreCombo = new ComboBox<String>();
		genreCombo.setPrefSize(130, 40);
		genreCombo.setItems(FXCollections.observableList(Genre.getOptions()));
		genreCombo.getSelectionModel().selectFirst();
		genreCombo.setFocusTraversable(false);
		genreBox.getChildren().add(genreCombo);

		searchPnl.add(genreBox, 1, 2);

	}

	private void initRatingCombo() {
		VBox ratingBox = new VBox(15);
		ratingBox.setPadding(new Insets(20, 20, 0, 0));

		Text rating = new Text("Rating: ");
		rating.setFont(ARIMO_BOLD14);
		rating.setFill(Color.rgb(90, 90, 90));
		ratingBox.getChildren().add(rating);

		ratingCombo = new ComboBox<String>();
		ArrayList<String> ratingOptions = new ArrayList<String>();
		ratingOptions.add("All");
		for (int i = 9; i > 0; i--) {
			ratingOptions.add(Integer.toString(i) + "+");
		}

		ratingCombo.setItems(FXCollections.observableList(ratingOptions));
		ratingCombo.getSelectionModel().selectFirst();
		ratingCombo.setPrefSize(130, 40);
		ratingCombo.setFocusTraversable(false);
		ratingBox.getChildren().add(ratingCombo);

		searchPnl.add(ratingBox, 2, 2);

	}

	private void initSortByCombo() {
		VBox sortByBox = new VBox(15);
		sortByBox.setPadding(new Insets(20, 20, 0, 0));

		Text sortBy = new Text("Sort By: ");
		sortBy.setFont(ARIMO_BOLD14);
		sortBy.setFill(Color.rgb(90, 90, 90));
		sortByBox.getChildren().add(sortBy);

		sortByCombo = new ComboBox<String>();
		sortByCombo.setPrefSize(130, 40);
		sortByCombo.setItems(FXCollections.observableList(SortBy.getOptions()));
		sortByCombo.getSelectionModel().selectFirst();
		sortByCombo.setFocusTraversable(false);
		sortByBox.getChildren().add(sortByCombo);

		searchPnl.add(sortByBox, 3, 2);

	}

	private void initSearchBtn() {
		Font arimoBold16 = Font.loadFont("File:assets/fonts/arimo/Arimo-Bold.ttf", 16);
		Background defaultCol = new Background(
				new BackgroundFill(Color.rgb(106, 192, 69, 1f), new CornerRadii(3), null));

		searchBtn = new Button("Search");
		searchBtn.setBackground(defaultCol);
		searchBtn.setFont(arimoBold16);
		searchBtn.setTextFill(Color.WHITE);
		searchBtn.setPrefSize(100, 40);

		searchBtn.setOnMouseEntered(mouseEvent -> {
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
					searchBtn.setBackground(new Background(new BackgroundFill(vColor, new CornerRadii(3), null)));
				}
			};
			animation.play();
		});

		searchBtn.setOnMouseExited(mouseEvent -> {
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
					searchBtn.setBackground(new Background(new BackgroundFill(vColor, new CornerRadii(3), null)));
				}
			};
			animation.play();
		});

		searchBtn.setOnAction(buttonEvent -> {
			submitSearch();
		});

		searchPnl.add(searchBtn, 5, 1);
	}

	private static void initNavBtns() {

		if (moviePnl.getChildren().size() >= 2) {
			navBtnsBox = (HBox) moviePnl.getChildren().get(1);
			Platform.runLater(() -> {
				navBtnsBox.getChildren().clear();
			});
		} else {
			navBtnsBox = new HBox(5);
		}

		if (moviePnl.getChildren().size() < 2) {
			Platform.runLater(() -> {
				moviePnl.getChildren().add(navBtnsBox);
			});
		}

		// setting alignment and padding
		navBtnsBox.setPadding(new Insets(0, 0, 20, 0));
		navBtnsBox.setAlignment(Pos.TOP_CENTER);

		if (!MovieCatalog.instance().getConnectionStatus()) {
			System.out.println("Returned because bad connection");
			return;
		}

		int numPages;
		if (instance == null) {
			// approximate page number based on past queries
			numPages = 2000;
		} else {
			numPages = instance.getNumPages();
		}

		if (numPages > 1) {

			// transparent and green colors for hover affect
			Background defaultCol = new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(3), null));
			Background onHoverCol = new Background(
					new BackgroundFill(Color.rgb(106, 192, 69), new CornerRadii(3), null));

			// Only add the "first" button if the page number is greater than or equal to
			// 3.
			if (getPageNum() >= 3) {
				Button navFirst = new Button("« First");
				navFirst.setBackground(defaultCol);
				navFirst.setFont(ARIMO_BOLD14);
				navFirst.setTextFill(Color.WHITE);
				navFirst.setBorder(new Border(new BorderStroke(Color.rgb(51, 51, 51), BorderStrokeStyle.SOLID,
						new CornerRadii(3), new BorderWidths(1))));
				navFirst.setMinSize(65, 40);

				navFirst.setOnMouseEntered(mouseEvent -> {
					btnFadeIn(mouseEvent);
				});

				navFirst.setOnMouseExited(mouseEvent -> {
					btnFadeOut(mouseEvent);
				});

				navFirst.setOnAction(mouseEvent -> {
					fireNavButton(navFirst);
				});

				Platform.runLater(() -> {
					navBtnsBox.getChildren().add(navFirst);
				});
			}

			// Only add the "previous" button if the page number is greater than 1.
			if (getPageNum() > 1) {
				Button navPrevious = new Button("« Previous");
				navPrevious.setBackground(defaultCol);
				navPrevious.setFont(ARIMO_BOLD14);
				navPrevious.setTextFill(Color.WHITE);
				navPrevious.setBorder(new Border(new BorderStroke(Color.rgb(51, 51, 51), BorderStrokeStyle.SOLID,
						new CornerRadii(3), new BorderWidths(1))));
				navPrevious.setPrefSize(92, 40);

				navPrevious.setOnMouseEntered(mouseEvent -> {
					btnFadeIn(mouseEvent);
				});

				navPrevious.setOnMouseExited(mouseEvent -> {
					btnFadeOut(mouseEvent);
				});

				navPrevious.setOnAction(mouseEvent -> {
					fireNavButton(navPrevious);
				});

				Platform.runLater(() -> {
					navBtnsBox.getChildren().add(navPrevious);
				});
			}

			// Adding page buttons based on the number of pages to be displayed. The maximum
			// number of pages is 8.
			Button[] numButtons;
			if (numPages <= 8) {
				numButtons = new Button[numPages];
			} else {
				numButtons = new Button[8];
			}

			int startNum = (int) ((Math.floor(((float) getPageNum()) / 8f)) * 8);
			if (startNum == getPageNum()) {
				startNum--;
			}

			for (int i = startNum; i < numButtons.length + startNum; i++) {
				numButtons[i - startNum] = new Button(Integer.toString(i + 1));
				numButtons[i - startNum].setFont(ARIMO_BOLD14);
				numButtons[i - startNum].setTextFill(Color.WHITE);
				numButtons[i - startNum].setBorder(new Border(new BorderStroke(Color.rgb(51, 51, 51),
						BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(1))));
				numButtons[i - startNum].setMinWidth(34);
				numButtons[i - startNum].setPrefHeight(40);

				if (getPageNum() != i + 1) {
					numButtons[i - startNum].setBackground(defaultCol);
					numButtons[i - startNum].setOnMouseEntered(mouseEvent -> {
						btnFadeIn(mouseEvent);
					});

					numButtons[i - startNum].setOnMouseExited(mouseEvent -> {
						btnFadeOut(mouseEvent);
					});
				} else {
					numButtons[i - startNum].setBackground(onHoverCol);
				}

				numButtons[i - startNum].setOnAction(mouseEvent -> {
					fireNavButton((Button) mouseEvent.getSource());
				});

				Button curr = numButtons[i - startNum];

				Platform.runLater(() -> {
					navBtnsBox.getChildren().add(curr);
				});
			}

			// Signifies that there are more pages to be displayed after the current batch
			// of 8
			if (getPageNum() + 7 < numPages) {
				Button moreAhead = new Button("...");
				moreAhead
						.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(3), null)));
				moreAhead.setFont(ARIMO_BOLD14);
				moreAhead.setTextFill(Color.WHITE);
				moreAhead.setBorder(new Border(new BorderStroke(Color.rgb(51, 51, 51), BorderStrokeStyle.SOLID,
						new CornerRadii(3), new BorderWidths(1))));
				moreAhead.setPrefSize(33, 40);

				Platform.runLater(() -> {
					navBtnsBox.getChildren().add(moreAhead);
				});
			}

			// Only add this button if we are not on the last page.
			if (getPageNum() != numPages) {
				Button next = new Button("Next »");
				next.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(3), null)));
				next.setFont(ARIMO_BOLD14);
				next.setTextFill(Color.WHITE);
				next.setBorder(new Border(new BorderStroke(Color.rgb(51, 51, 51), BorderStrokeStyle.SOLID,
						new CornerRadii(3), new BorderWidths(1))));
				next.setPrefSize(65, 40);
				next.setMinHeight(40);

				next.setOnMouseEntered(mouseEvent -> {
					btnFadeIn(mouseEvent);
				});

				next.setOnMouseExited(mouseEvent -> {
					btnFadeOut(mouseEvent);
				});

				next.setOnAction(mouseEvent -> {
					fireNavButton(next);
				});

				Platform.runLater(() -> {
					navBtnsBox.getChildren().add(next);
				});
			}

		}
	}

	private static void btnFadeIn(MouseEvent mouseEvent) {
		final Animation animation = new Transition() {

			{
				setCycleDuration(Duration.millis(200));
				setInterpolator(Interpolator.EASE_BOTH);
			}

			@Override
			protected void interpolate(double frac) {

				Color vColor = Color.rgb(106, 192, 69, frac);
				((Button) mouseEvent.getSource())
						.setBackground(new Background(new BackgroundFill(vColor, new CornerRadii(3), null)));
			}
		};
		animation.play();
	}

	private static void btnFadeOut(MouseEvent mouseEvent) {
		final Animation animation = new Transition() {

			{
				setCycleDuration(Duration.millis(200));
				setInterpolator(Interpolator.EASE_BOTH);
			}

			@Override
			protected void interpolate(double frac) {

				Color vColor = Color.rgb(106, 192, 69, 1 - frac);
				((Button) mouseEvent.getSource())
						.setBackground(new Background(new BackgroundFill(vColor, new CornerRadii(3), null)));
			}
		};
		animation.play();
	}

	private static void fireNavButton(Button btn) {

		if (instance == null) {
			instance = MovieCatalog.instance();
		}

		BackgroundWorker.submit(() -> {

			Main.showBufferBar();

			try {
				if ("« First".equals(btn.getText())) {
					instance.setPageToFirst();
				} else if ("« Previous".equals(btn.getText())) {
					instance.previousPage();
				} else if ("Next »".equals(btn.getText())) {
					instance.nextPage();
				} else {
					try {
						int pageNum = Integer.parseInt(btn.getText());
						instance.setPageTo(pageNum);
						// Hopefully will never happen
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
				initNavBtns();
				// Also hopefully will never happen but more likely :)
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}

			// Hide the buffering bar
			Main.hideBufferBar();

		});

	}

	private static int getPageNum() {
		if (instance != null) {
			return instance.getPageNumber();
		} else {
			return 1;
		}
	}

	private void submitSearch() {
		String searchTerm = searchTermTxt.getText();
		searchTermTxt.clear();
		String quality = qualityCombo.getSelectionModel().getSelectedItem();
		String genre = genreCombo.getSelectionModel().getSelectedItem();
		String rating = ratingCombo.getSelectionModel().getSelectedItem();
		String sortBy = sortByCombo.getSelectionModel().getSelectedItem();

		SearchQuery searchQuery = SearchQuery.getSearchQuery(searchTerm, quality, genre, rating, sortBy, 1);

		BackgroundWorker.submit(() -> {
			
			Main.showBufferBar();

			try {
				instance.makeRequest(searchQuery);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}

			initNavBtns();

			// Switching scenes must be done on JavaFX thread.
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					// Hide the buffering bar here
					Main.hideBufferBar();
				}
			});

		});
	}

	public static GridPane loadMovies(boolean connectionOkay) {
		System.out.println("Connection Okay: " + connectionOkay);

		GridPane movieGrid;

		if (moviePnl.getChildren().size() > 2) {
			Platform.runLater(() -> {
				moviePnl.getChildren().remove(2);
			});
		}

		movieGrid = new GridPane();

		movieGrid.setHgap(55);
		movieGrid.setVgap(60);
		movieGrid.setAlignment(Pos.TOP_CENTER);
		movieGrid.setPadding(new Insets(0, 0, 20, 0));

		if (!connectionOkay) {
			VBox noConnectionBox = new VBox(10);
			noConnectionBox.setAlignment(Pos.TOP_CENTER);
			noConnectionBox.setMinHeight(280);

			System.out.println("connection was not okay and displayed!");

			// If displaying no connection page then don't show any nav btns
			if (moviePnl.getChildren().size() >= 2) {
				HBox navBtnsBox = (HBox) moviePnl.getChildren().get(1);
				navBtnsBox.getChildren().clear();
			}

			Label noConnectionLbl = new Label("Unable to establish a connection to YTS.mx. "
					+ "Please check your network connection and try again!");
			noConnectionLbl.setPrefWidth(600);
			noConnectionLbl.setWrapText(true);
			noConnectionLbl.setTextAlignment(TextAlignment.CENTER);
			noConnectionLbl.setFont(ARIMO_BOLD22);
			noConnectionLbl.setTextFill(Color.WHITE);
			noConnectionLbl.setEffect(new DropShadow(2, 0, 2, Color.rgb(0, 0, 0, 0.25f)));

			ImageView noConnectionIcon = new ImageView(new Image("file:assets/noConnection1.png"));
			GridPane.setHalignment(noConnectionIcon, HPos.CENTER);

			Font arimoBold16 = Font.loadFont("File:assets/fonts/arimo/Arimo-Bold.ttf", 16);
			Background defaultCol = new Background(
					new BackgroundFill(Color.rgb(106, 192, 69, 1f), new CornerRadii(3), null));

			Button reloadBtn = new Button("Reload");
			reloadBtn.setBackground(defaultCol);
			reloadBtn.setFont(arimoBold16);
			reloadBtn.setTextFill(Color.WHITE);
			reloadBtn.setPrefSize(100, 40);

			reloadBtn.setOnMouseEntered(mouseEvent -> {
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
						reloadBtn.setBackground(new Background(new BackgroundFill(vColor, new CornerRadii(3), null)));
					}
				};
				animation.play();
			});

			reloadBtn.setOnMouseExited(mouseEvent -> {
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
						reloadBtn.setBackground(new Background(new BackgroundFill(vColor, new CornerRadii(3), null)));
					}
				};
				animation.play();
			});

			reloadBtn.setOnAction(buttonEvent -> {
				if (instance.getConnectionStatus()) {
					try {
						instance.makeRequest(SearchQuery.getDefaultSearchQuery());
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
					initNavBtns();
				}
			});

			noConnectionBox.getChildren().addAll(noConnectionLbl, noConnectionIcon, reloadBtn);
			movieGrid.add(noConnectionBox, 0, 0);
			Platform.runLater(() -> {
				moviePnl.getChildren().add(movieGrid);
			});
			return null;
		}

		ArrayList<Movie> movies = instance.getCurrentPage();

		if (movies.isEmpty()) {
			Label noResults = new Label("Your search query yeilded no results");
			noResults.setFont(ARIMO_BOLD22);
			noResults.setTextFill(Color.WHITE);
			noResults.setEffect(new DropShadow(2, 0, 2, Color.rgb(0, 0, 0, 0.25f)));
			movieGrid.setAlignment(Pos.CENTER);
			movieGrid.add(noResults, 0, 0);
			Platform.runLater(() -> {
				moviePnl.getChildren().add(movieGrid);
			});
			return null;
		}

		// ****************END ERROR CASES *****************

		int cnt = 0;
		float rows = movies.size() / 5f;
		for (float i = 0; i < rows; i++) {
			for (int j = 0; j < ((movies.size() >= 5) ? 5 : movies.size()); j++) {
				if (cnt == movies.size()) {
					break;
				}
				// Movie object init
				Movie currentMovie = movies.get(cnt);
				// Main box containing the thumbnail, movie title, and year.
				VBox movieBox = new VBox(5);

				// Thumbnail image and image view inti
				Image thumbnail = currentMovie.getThumbnail();
				ImageView thumbnailView = new ImageView(thumbnail);
				thumbnailView.setFitWidth(170);
				thumbnailView.setFitHeight(255);

				// An overlay for onHover details. Shows genres and ratings and semi-transparent
				// darkening layer.
				StackPane imageContainer = new StackPane();
				imageContainer.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID,
						new CornerRadii(5), new BorderWidths(5))));
				imageContainer.getChildren().add(thumbnailView);

				// Semi-transparent layer.
				Region onHoverRegion = new Region();
				onHoverRegion.setPrefSize(170, 255);
				onHoverRegion
						.setBackground(new Background(new BackgroundFill(Color.rgb(29, 29, 29, 0.80), null, null)));
				onHoverRegion.setVisible(false);
				imageContainer.getChildren().add(onHoverRegion);

				// A VBox containing labels for rating and genre of currentMovie
				VBox ratingGenreBox = new VBox(15);
				ratingGenreBox.setAlignment(Pos.TOP_CENTER);
				ratingGenreBox.setOpacity(0);

				// Cute little star icon for rating :)
				HBox starIcon = new HBox();
				starIcon.setAlignment(Pos.CENTER);
				starIcon.getChildren().add(new ImageView(new Image("File:assets/star.png")));
				starIcon.setPadding(new Insets(25, 0, 0, 0));
				ratingGenreBox.getChildren().add(starIcon);

				// Label for movie rating.
				float ratingStr = currentMovie.getRating();
				// Some movies don't have ratings
				if (ratingStr != 0) {

					Label rating = new Label(currentMovie.getRating() + " / 10");
					rating.setFont(ARIMO_BOLD22);
					rating.setTextFill(Color.WHITE);
					rating.setPadding(new Insets(0, 0, 20, 0));
					ratingGenreBox.getChildren().add(rating);
				}

				String[] genresArr = currentMovie.getGenres();
				if (genresArr != null) {
					// Label 1 for genre
					Label genre1 = new Label(genresArr[0]);
					genre1.setFont(ARIMO_BOLD22);
					genre1.setTextFill(Color.WHITE);
					ratingGenreBox.getChildren().add(genre1);

					// Movies have at least one genre if the arr is not null.
					if (genresArr.length > 1) {
						// Label 2 for genre
						Label genre2 = new Label(genresArr[1]);
						genre2.setFont(ARIMO_BOLD22);
						genre2.setTextFill(Color.WHITE);
						ratingGenreBox.getChildren().add(genre2);
					}
				}

				imageContainer.getChildren().add(ratingGenreBox);

				imageContainer.setOnMouseEntered(mouseEvent -> {
					StackPane container = (StackPane) mouseEvent.getSource();

					container.setBorder(new Border(new BorderStroke(Color.rgb(106, 192, 69), BorderStrokeStyle.SOLID,
							new CornerRadii(5), new BorderWidths(5))));

					container.getChildren().get(1).setVisible(true);

					// Transition stuff
					final Animation animation = new Transition() {
						{
							setCycleDuration(Duration.millis(300));
							setInterpolator(Interpolator.EASE_BOTH);
						}

						@Override
						protected void interpolate(double frac) {
							StackPane container = (StackPane) mouseEvent.getSource();
							container.getChildren().get(2).setOpacity(frac);

						}
					};
					animation.play();
				});

				imageContainer.setOnMouseExited(mouseEvent -> {
					StackPane container = (StackPane) mouseEvent.getSource();
					container.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID,
							new CornerRadii(5), new BorderWidths(5))));

					container.getChildren().get(1).setVisible(false);

					// Transition stuff
					final Animation animation = new Transition() {

						{
							setCycleDuration(Duration.millis(300));
							setInterpolator(Interpolator.EASE_BOTH);
						}

						@Override
						protected void interpolate(double frac) {
							StackPane container = (StackPane) mouseEvent.getSource();
							container.getChildren().get(2).setOpacity(1 - frac);

						}
					};
					animation.play();

				});
				HBox movieTitleContainer = new HBox(5);
				movieTitleContainer.setMaxWidth(thumbnailView.getFitWidth());

				if (!"en".equals(currentMovie.getLang())) {
					Text movieLangText = new Text("[" + currentMovie.getLang().toUpperCase() + "]");
					movieLangText.setFont(ARIMO_BOLD12);
					movieLangText.setFill(Color.rgb(172, 215, 222));
					movieTitleContainer.getChildren().add(movieLangText);
				}

				Label movieTitle = new Label(currentMovie.getTitle());
				movieTitle.setFont(ARIMO_BOLD14);
				movieTitle.setTextFill(Color.WHITE);
				movieTitleContainer.getChildren().add(movieTitle);

				Label movieYear = new Label(Integer.toString(currentMovie.getYear()));
				movieYear.setFont(ARIMO_REG12);
				movieYear.setTextFill(Color.rgb(145, 145, 145));

				movieBox.getChildren().addAll(imageContainer, movieTitleContainer, movieYear);

				movieBox.setOnMouseClicked(mouseEvent -> {
					if (instance.getConnectionStatus()) {

						// Initializing the MovieInfoPnl in a background thread to prevent GUI from
						// hanging on click. The MovieInfoPnl constructor makes an HTTPClient call which
						// takes a second and then parses the response which takes even longer.
						BackgroundWorker.submit(new Runnable() {
							@Override
							public void run() {
								Main.showBufferBar();

								MovieInfoPnl infoPnl = new MovieInfoPnl(currentMovie);
								// Switching scenes must be done on JavaFX thread.

								// Hide the buffering bar here
								Main.hideBufferBar();

								Main.switchSceneContent(infoPnl);

							}
						});

					} else
						loadMovies(instance.getConnectionStatus());
				});

				movieGrid.add(movieBox, j, (int) i);

				cnt++;
			}
		}

		Platform.runLater(() -> {
			moviePnl.getChildren().add(movieGrid);
		});
		return movieGrid;
	}

	public static void updateMovieGrid(GridPane movieGrid) {
		System.out.println("Updated grid");

		if (moviePnl.getChildren().size() > 2)
			Platform.runLater(() -> {
				moviePnl.getChildren().remove(2);
			});

		Platform.runLater(() -> {
			moviePnl.getChildren().add(movieGrid);
		});
	}
}
