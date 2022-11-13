package yify.model.moviecatalog;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javafx.scene.image.Image;
import yify.model.movie.Movie;
import yify.model.moviecatalog.searchquery.SearchQuery;
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
public class MovieCatalog {
	/**
	 * A thread that checks the connection between the client and the server at a
	 * fixed interval of time.
	 */
	public ConnectionThread connectionThread;
	/* A boolean denoting the current status of the client-server connection */
	private static boolean connectionOkay = true;
	/** The singleton instance for the MovieCatalog class. */
	private static MovieCatalog instance;
	/**
	 * The ArrayList holding the current page of parsed Movie objects in the
	 * catalog.
	 */
	private ArrayList<Movie> currentPage;
	/** The unparsed response from the YTS.mx server. */
	private JsonObject rawPage;
	/**
	 * A search query made by the user containing various parameters and their
	 * values to be sent to the YTS.mx servers. /** An HttpClient to be used to make
	 * HTTP requests.
	 */
	private SearchQuery searchQuery;
	/** An HTTPClient used to make HTTP requests to the YTS.mx servers. */
	protected static HttpClient client = HttpClient.newHttpClient();
	/** The known URL of the YTS.mx list of movies provided by the API */
	public static final String LIST_MOVIES = "https://yts.torrentbay.to/api/v2/list_movies.json/";

	/** The parts of the known YTS.mx URL split up */
	public static final String SCHEME = "https";
	public static final String HOST = "yts.torrentbay.to";
	public static final String PATH = "/api/v2/list_movies.json/";

	/** A constant for the page parameter as per the YTS.mx API */
	public static final String PAGE_PARAM = "page=";
	/** A constant for the search parameter as per the YTS.mx API */
	public static final String SEARCH_PARAM = "query_term=";
	/** A constant for the quality parameter as per the YTS.mx API */
	public static final String QUALITY_PARAM = "quality=";
	/** A constant for the genre parameter as per the YTS.mx API */
	public static final String GENRE_PARAM = "genre=";
	/** A constant for the rating parameter as per the YTS.mx API */
	public static final String RATING_PARAM = "minimum_rating=";
	/** A constant for the sort by parameter as per the YTS.mx API */
	public static final String SORT_PARAM = "sort_by=";

	/**
	 * Can be used to obtain the static instance of the MovieCatalog class.
	 * 
	 */
	public static MovieCatalog instance() {
		if (instance == null) {
			instance = new MovieCatalog();
		}

		return instance;
	}

	/**
	 * Constructs a new MovieCatalog object and sets the current page of the catalog
	 * to page 1.
	 */
	private MovieCatalog() {
		connectionThread = new ConnectionThread(10);
		currentPage = new ArrayList<Movie>();

	}

	public void makeRequest(SearchQuery searchQuery) throws IOException, InterruptedException {
		String searchTerm = searchQuery.getSearchTerm();
		String quality = searchQuery.getQuality();
		String genre = searchQuery.getGenre();
		int rating = searchQuery.getRating();
		String sortBy = searchQuery.getSortBy();
		int pageNum = searchQuery.getPageNum();
		
		makeRequest(searchTerm, quality, genre, Integer.toString(rating), sortBy, pageNum);
		this.searchQuery = searchQuery;
	}

	public static void setConnectionOkay(boolean connectionOkayPar) {
		connectionOkay = connectionOkayPar;
	}

	public boolean getConnectionStatus() {
		return connectionOkay;
	}

	public int getPageNumber() {
		return rawPage.get("page_number").getAsInt();
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
		

//		String requestString = LIST_MOVIES + "?" + SEARCH_PARAM + searchTerm + "&" + QUALITY_PARAM + quality + "&"
//				+ GENRE_PARAM + genre + "&" + RATING_PARAM + ("ALl".equals(rating) ? "0" : rating) + "&" + SORT_PARAM
//				+ sortBy + PAGE_PARAM + pageNum;

		String queryString = SEARCH_PARAM + searchTerm + "&" + QUALITY_PARAM + quality + "&" + GENRE_PARAM + genre + "&"
				+ RATING_PARAM + ("ALl".equals(rating) ? "0" : rating) + "&" + SORT_PARAM + sortBy + "&" + PAGE_PARAM
				+ pageNum;

		URI uri = null;
		try {
			uri = new URI(SCHEME, HOST, PATH, queryString, null);
		} catch (URISyntaxException e) {
			// should hopefully never happen.
			e.printStackTrace();
		}

		HttpResponse<String> response = null;

		try {
			// Create a request
			HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(10)).build();

			// Use the client to send the request
			response = client.send(request, BodyHandlers.ofString());
		} catch (Exception e) {
			// Any exceptions that occur here are most likely network exceptions so we
			// assume that the connection is bad.
			e.printStackTrace();
			setConnectionOkay(false);
			BrowserPnl.loadMovies(connectionOkay);
			return;
		}

		setConnectionOkay(true);

		// the response:
		String jsonString = "";
		if (response != null)
			jsonString = response.body();
		else
			// Should hopefully never happen.
			return;

//		System.out.println(jsonString);
		System.out.println(uri.toString());

		rawPage = new Gson().fromJson(jsonString, JsonObject.class).get("data").getAsJsonObject();
		parseRawPage();
		BrowserPnl.loadMovies(connectionOkay);

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

		System.out.println("Done!");

	}

	private String[] parseGenres(JsonArray genresJson) {
		String[] genres = new String[genresJson.size()];

		for (int i = 0; i < genres.length; i++) {
			genres[i] = genresJson.get(i).getAsString();
		}

		return genres;
	}

	private int getLimit() {
		return rawPage.get("limit").getAsInt();
	}

	private int getMovieCount() {
		return rawPage.get("movie_count").getAsInt();
	}

}
