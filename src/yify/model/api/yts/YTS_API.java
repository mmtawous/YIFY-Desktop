package yify.model.api.yts;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.scene.image.Image;
import yify.model.api.yts.searchquery.SearchQuery;
import yify.model.movie.Movie;
import yify.model.movie.PageState;
import yify.view.ui.App;
import yify.view.ui.BrowserPnl;

/**
 * A class holding individual movies in an ArrayList. Movies are loaded in
 * groups of 20 from the YTS.mx servers and are added into the list. A user may
 * traverse the catalog by using the respective getter methods, e.g.
 * getNextPage(), getCurrentPage(), getPreviousPage(). The MovieCatalog class
 * follows the singleton pattern and may not be constructed via normal means,
 * users should call the instance() method instead.
 * 
 * @author Mohamed Tawous
 *
 */
public class YTS_API {
	/** The singleton instance for the MovieCatalog class. */
	private static YTS_API instance;

	/**
	 * The ArrayList holding the current page of parsed Movie objects in the
	 * catalog.
	 */
	private static ArrayList<Movie> currentPage;

	/**
	 * Stores instances of GridPane representing pages that have been previously
	 * loaded. The key is the ID of the page and the value is the page.
	 */
	private static LinkedHashMap<Integer, PageState> loaded;

	/** The unparsed response from the YTS.mx server. */
	private static JsonObject rawPage;
	/**
	 * A search query made by the user containing various parameters and their
	 * values to be sent to the YTS.mx servers. /** An HttpClient to be used to make
	 * HTTP requests.
	 */
	private static SearchQuery searchQuery;
	/** An HTTPClient used to make HTTP requests to the YTS.mx servers. */
	protected static HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
	

	/** The parts of the known YTS.mx URL split up */
	public static final String SCHEME = "https";
	public static final String HOST = "yts.mx";
	public static final String LIST_MOVIES = "/api/v2/list_movies.json/";
	
	public static final String MOVIE_DETAILS = "/api/v2/movie_details.json/";
	/** A constant for the id parameter as per the YTS.mx API */
	public static final String ID_PARAM = "movie_id=";
	/** A constant for the with_cast parameter as per the YTS.mx API */
	public static final String WITH_CAST_PARAM = "with_cast=";
	/** A constant for the with_images parameter as per the YTS.mx API */
	public static final String WITH_IMAGES_PARAM = "with_images=";


	/**
	 * Can be used to obtain the static instance of the MovieCatalog class.
	 * 
	 */
	public static YTS_API instance() {
		if (instance == null) {
			instance = new YTS_API();
		}

		return instance;
	}

	/**
	 * Constructs a new MovieCatalog object and sets the current page of the catalog
	 * to page 1.
	 */
	private YTS_API() {
		new ConnectionThread(10); // Schedule connection checking for 10 second intervals
		currentPage = new ArrayList<Movie>();

		loaded = new LinkedHashMap<Integer, PageState>(10, 0.75f, true) {
			private static final long serialVersionUID = 1L;
			private static final int MAX_ENTRIES = 10;

			@Override
			protected boolean removeEldestEntry(Map.Entry<Integer, PageState> eldest) {
				return (size() > MAX_ENTRIES);
			}

		};

	}

	public void makeRequest(SearchQuery searchQuery) throws IOException, InterruptedException {
		String searchTerm = searchQuery.getSearchTerm();
		String quality = searchQuery.getQuality();
		String genre = searchQuery.getGenre();
		int rating = searchQuery.getRating();
		String sortBy = searchQuery.getSortBy();
		int pageNum = searchQuery.getPageNum();

		YTS_API.searchQuery = searchQuery;

		makeRequest(searchTerm, quality, genre, Integer.toString(rating), sortBy, pageNum);
	}


	public int getPageNumber() {
		return searchQuery.getPageNum();
	}

	public int getNumPages() {
		float limit = getLimit();
		float movieCnt = getMovieCount();

		return (int) Math.ceil(movieCnt / limit);
	}

	public void nextPage() throws IOException, InterruptedException {
		searchQuery.nextPage();
		makeRequest(searchQuery);
	}

	public void previousPage() throws IOException, InterruptedException {
		searchQuery.previousPage();
		makeRequest(searchQuery);
	}

	public void setPageTo(int pageNum) throws IOException, InterruptedException {
		searchQuery.setPageNum(pageNum);
		makeRequest(searchQuery);
	}

	public void setPageToFirst() throws IOException, InterruptedException {
		setPageTo(1);
	}

	public ArrayList<Movie> getCurrentPage() {
		return currentPage;
	}

	public static HttpClient getClient() {
		return client;
	}

	/**
	 * A helper method that requests the movie list based on a number of search
	 * parameters.
	 * 
	 * @param searchTerm the searchTerm of the search query
	 * @param quality    the quality chosen by the user (can be left as "All")
	 * @param genre      the genre chosen by the user (can be left as "All")
	 * @param rating     the rating chosen by the user (can be left as "All")
	 * @param sortBy     the way that movies should be sorted in the server response
	 * @param pageNum    the page number of the search query. The same query can be
	 *                   made more than once with different page numbers to yield
	 *                   different results match the search query.
	 * @throws IOException          if an I/O error occurs when sending or receiving
	 * @throws InterruptedException if the operation is interrupted
	 */
	private void makeRequest(String searchTerm, String quality, String genre, String rating, String sortBy, int pageNum)
			throws IOException, InterruptedException {

		String queryString = searchQuery.getUrlString();

		// Check if have loaded this page before. If we have then just display it and
		// finish early
		PageState result = loaded.get(queryString.hashCode());
		if (result != null && ConnectionThread.getConnectionStatus()) {
			BrowserPnl.updateMovieGrid(result.getMovieGrid());
			return;
		}

		URI uri = null;
		try {
			uri = new URI(SCHEME, HOST, LIST_MOVIES, queryString, null);
		} catch (URISyntaxException e) {
			// should hopefully never happen.
			e.printStackTrace();
		}

		HttpResponse<String> response = null;

		try {
			// Create a request
			HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(5)).build();
			
			// Use the client to send the request
			response = client.send(request, BodyHandlers.ofString());
		} catch (Exception e) {
			// Any exceptions that occur here are most likely network exceptions so we
			// assume that the connection is bad.
			e.printStackTrace();
			ConnectionThread.setConnectionOkay(false);

			// This will just display the network error page
			BrowserPnl.loadMovies(ConnectionThread.getConnectionStatus());
			return;
		}

		ConnectionThread.setConnectionOkay(true);

		// the response:
		String jsonString = "";
		if (response != null)
			jsonString = response.body();
		else
			// Should hopefully never happen.
			return;

		System.out.println(uri.toString());

		rawPage = new Gson().fromJson(jsonString, JsonObject.class).get("data").getAsJsonObject();
		parseRawPage();

		// Saving the returned page in a HashMap.
		// This allows for quick to a page that has already been loaded.

		// This could be dangerous if a user loads too many pages, memory problems will
		// happen. We need to discard the least recently accessed page if we are storing
		// over a certain threshold of pages. (Done in constructor using LinkedHashMap
		// API)
		loaded.put(queryString.hashCode(), new PageState(BrowserPnl.loadMovies(ConnectionThread.getConnectionStatus()),
				rawPage.get("limit").getAsInt(), rawPage.get("movie_count").getAsInt()));

	}

	private void parseRawPage() {
		// clearing before each search query
		if (!currentPage.isEmpty()) {
			currentPage.clear();
		}

		// If the search doesn't return anything the JSON response will be missing the
		// movies array so this code can throw an exception.
		JsonArray moviesInPage = rawPage.getAsJsonArray("movies");
		if (moviesInPage == null) {
			return;
		}

		for (int i = 0; i < moviesInPage.size(); i++) {
			JsonObject currentMov = moviesInPage.get(i).getAsJsonObject();

			int id = currentMov.getAsJsonPrimitive("id").getAsInt();
			Image thumbnail = new Image(currentMov.getAsJsonPrimitive("medium_cover_image").getAsString(), true);
			String title = currentMov.getAsJsonPrimitive("title_english").getAsString();
			int year = currentMov.getAsJsonPrimitive("year").getAsInt();
			float rating = currentMov.getAsJsonPrimitive("rating").getAsFloat();
			String lang = currentMov.getAsJsonPrimitive("language").getAsString();

			JsonArray genresJson = currentMov.getAsJsonArray("genres");
			String[] genres = null;
			if (genresJson != null) {
				genres = parseGenres(genresJson);
			}

			currentPage.add(new Movie(id, thumbnail, title, year, rating, genres, lang));
		}

	}

	private String[] parseGenres(JsonArray genresJson) {
		String[] genres = new String[genresJson.size()];

		for (int i = 0; i < genres.length; i++) {
			genres[i] = genresJson.get(i).getAsString();
		}

		return genres;
	}

	private int getLimit() {
		return loaded.get(searchQuery.getUrlString().hashCode()).getPageLimit();
	}

	private int getMovieCount() {
		return loaded.get(searchQuery.getUrlString().hashCode()).getMovieCount();
	}
	
	
	
	public JsonObject getMovieDetails(Movie movie) throws IOException, InterruptedException {
		HttpClient client = YTS_API.getClient();

		String queryString = ID_PARAM + movie.getId() + "&" + WITH_IMAGES_PARAM + true + "&" + WITH_CAST_PARAM + true;

		URI uri = null;
		try {
			uri = new URI(SCHEME, HOST, MOVIE_DETAILS, queryString, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		HttpResponse<String> response = null;

		try {
			// Create a request
			HttpRequest request = HttpRequest.newBuilder(uri).timeout(java.time.Duration.ofSeconds(5)).build();

			// Use the client to send the request
			response = client.send(request, BodyHandlers.ofString());
		} catch (Exception e) {
			// Any exceptions that occur here are most likely network exceptions so we
			// assume that the connection is bad.
			ConnectionThread.setConnectionOkay(false);
			BrowserPnl.loadMovies(false);
			App.hideBufferBar();
			return null;
		}

		ConnectionThread.setConnectionOkay(true);

		// the response:
		String jsonString = "";
		if (response != null)
			jsonString = response.body();
		else
			// Should hopefully never happen.
			return null;
		// System.out.println(jsonString);
		System.out.println(uri.toString());

		JsonObject rawPage = new Gson().fromJson(jsonString, JsonObject.class).getAsJsonObject("data")
				.getAsJsonObject("movie");
		return rawPage;
	}

}
