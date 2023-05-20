package yify.view.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.commons.text.similarity.LevenshteinDistance;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import yify.model.api.yts.ConnectionThread;
import yify.model.api.yts.YTS_API;
import yify.model.api.yts.util.SearchResult;
import yify.model.movie.Movie;
import yify.view.ui.util.BackgroundWorker;

public class SearchBar extends VBox {

	private static final int CELL_HEIGHT = 25;

	private ObservableList<String> suggestions = FXCollections.observableArrayList();
	private ListView<String> suggestionsList = new ListView<>(suggestions);
	private TextField searchBar = new TextField();
	private SimpleBooleanProperty listShouldBeVisible = new SimpleBooleanProperty(false);

	public SearchBar(LinkedHashMap<String, Movie> dictionary) {
		// TODO: Listview flickers when it has been hidden and then reshown.

		searchBar.setPromptText("Search...");

		suggestionsList.setId("suggestionsList");
		suggestionsList.setVisible(false);
		suggestionsList.setMinHeight(0);
		suggestionsList.prefWidthProperty().bind(searchBar.prefWidthProperty());
//		suggestionsList.maxWidthProperty().bind(searchBar.maxWidthProperty());
//		suggestionsList.minWidthProperty().bind(searchBar.maxWidthProperty());

//		if (event.getCode() == KeyCode.DOWN && searchBar.getCaretPosition() == searchBar.getText().length()) {
//			if (suggestionsList.getSelectionModel().getSelectedItem() == null) {
//				suggestionsList.requestFocus();
//				suggestionsList.getSelectionModel().clearAndSelect(0);
//			} else
//				suggestionsList.getSelectionModel().selectNext();
//
//		} else if (event.getCode() == KeyCode.UP && searchBar.getCaretPosition() == 0) {
//			if (suggestionsList.getSelectionModel().getSelectedItem() == null) {
//				suggestionsList.requestFocus();
//				suggestionsList.getSelectionModel().clearAndSelect(0);
//			} else
//				suggestionsList.getSelectionModel().selectPrevious();
//			
//		} else 

		searchBar.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				App.getBrowserPnl().requestFocus();
				listShouldBeVisible.set(false);
			}
		});

		searchBar.setOnKeyTyped(event -> {

			// Update suggestion list
			String text = searchBar.getText();

			synchronized (dictionary) {
				search(dictionary.keySet(), text);
			}

			listShouldBeVisible.set(!suggestions.isEmpty() && !text.isEmpty());

			if (!suggestions.isEmpty() && !text.isEmpty()) {
				updateSuggestionListHeight();
			}

		});

		suggestionsList.visibleProperty()
				.bind(searchBar.focusedProperty().and(listShouldBeVisible).or(suggestionsList.focusedProperty()));

		suggestionsList.visibleProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				// If the visibility was set to false clear the suggestions and update height
				if (!newValue) {
					suggestions.clear();
					listShouldBeVisible.set(false);
					updateSuggestionListHeight();
				} else {
					updateSuggestionListHeight();
				}
			}
		});

		suggestionsList.setOnMouseClicked(event -> {
			submitSelected();
		});

		this.getChildren().addAll(searchBar, suggestionsList);
	}

	private void submitSelected() {
		if (ConnectionThread.getConnectionStatus()) {

			// Initializing the MovieInfoPnl in a background thread to prevent GUI from
			// hanging on click. The MovieInfoPnl constructor makes an HTTPClient call which
			// takes a second and then parses the response which takes even longer.
			BackgroundWorker.submit(new Runnable() {
				@Override
				public void run() {
					App.showBufferBar();

					String selectedTitle = suggestionsList.getSelectionModel().getSelectedItem();
					if (selectedTitle != null) {
						searchBar.setText(selectedTitle);
					}

					Movie selectedMovie = YTS_API.instance().getSingleMovie(selectedTitle);

					if (selectedMovie != null) {
						MovieInfoPnl infoPnl = new MovieInfoPnl(selectedMovie);
						App.switchSceneContent(infoPnl);
					}

					// Hide the buffering bar here
					App.hideBufferBar();

				}
			});

		} else
			BrowserPnl.loadMovies(ConnectionThread.getConnectionStatus());
	}

	private synchronized void updateSuggestionListHeight() {

		final double start = suggestionsList.getHeight() == -0.0 ? 0.0 : suggestionsList.getHeight();
		final double target = (suggestions.size() * CELL_HEIGHT);
		final double diff = start - target;

		final Animation animation = new Transition() {

			{
//				System.out.println("start: " + start);
//				System.out.println("target: " + target);
//				System.out.println("diff: " + diff);

				setCycleDuration(Duration.millis(250));
				setInterpolator(Interpolator.EASE_OUT);
			}

			@Override
			protected void interpolate(double frac) {
				try {
					suggestionsList.setPrefHeight(start - (diff * frac));
					suggestionsList.setMinHeight(start - (diff * frac));
					suggestionsList.setMaxHeight(start - (diff * frac));

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		};
		animation.play();

	}

	// I think we are at a decent spot. maybe try to incorporate tokenization.
	// trouble query was "harry potter and the phoenix"
	private void search(Set<String> titleSet, String query) {
		ArrayList<SearchResult> result = new ArrayList<SearchResult>();
		LevenshteinDistance ld = new LevenshteinDistance(1);
		String santinzedQ = sanitize(query);

		System.out.println("Size: " + titleSet.size());

		for (String title : titleSet) {
			String santinzedT = sanitize(title);

			int distance = ld.apply(santinzedQ, santinzedT);
			if (distance >= 0) {
				if (santinzedT.startsWith(santinzedQ) || distance == 0) {
					distance--;
				}

				result.add(new SearchResult(title, distance));
			} else if (santinzedT.contains(santinzedQ)) {
				result.add(new SearchResult(title, 0));
			}
		}

		// Sort the list by distance to display the better matches first
		Collections.sort(result);

		suggestions.clear();
		for (int i = 0; i < 5 && i < result.size(); i++) {
			suggestions.add(result.get(i).getTitle());
		}
	}

	private static String sanitize(String str) {
		StringBuilder build = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isAlphabetic(c) || Character.isDigit(c)) {
				build.append(Character.toLowerCase(c));
			}
		}

		return build.toString();
	}

	public TextField getSearchBar() {
		return searchBar;
	}

//	private List<String> getMatchingSuggestions(String prefix) {
//		if (prefix.isEmpty()) {
//			return SUGGESTIONS;
//		}
//		return SUGGESTIONS.stream().filter(suggestion -> suggestion.toLowerCase().startsWith(prefix.toLowerCase()))
//				.toList();
//	}

}
