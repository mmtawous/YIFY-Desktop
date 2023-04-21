package yify.model.api.yts.searchquery;

public class SearchQuery {
	private Quality quality;
	private int rating;
	private String searchTerm;
	private Genre genre;
	private SortBy sortBy;
	private int pageNum;
	
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

	private SearchQuery(String searchTerm, Quality quality, Genre genre, int rating, SortBy sortBy, int pageNum) {
		setQuality(quality);
		setRating(rating);
		setSearchTerm(searchTerm);
		setGenre(genre);
		setSortBy(sortBy);
		setPageNumConst(pageNum);
	}
	
	public String getUrlString() {
		return SEARCH_PARAM + searchTerm + "&" + QUALITY_PARAM + getQuality() + "&" + GENRE_PARAM + getGenre() + "&"
				+ RATING_PARAM + rating + "&" + SORT_PARAM + getSortBy() + "&" + PAGE_PARAM
				+ pageNum;
	}

	public int getPageNum() {
		return pageNum;
	}
	
	private void setPageNumConst(int pageNum) {
		if (pageNum > 0) {
			this.pageNum = pageNum;
		} else {
			throw new IllegalArgumentException();
		}
		
	}

	public SearchQuery setPageNum(int pageNum) {
		if (pageNum > 0) {
			this.pageNum = pageNum;
		} else {
			throw new IllegalArgumentException();
		}
		
		return new SearchQuery(searchTerm, quality, genre, rating, sortBy, pageNum);
	}

	public SearchQuery nextPage() {
		this.pageNum++;
		
		return new SearchQuery(searchTerm, quality, genre, rating, sortBy, pageNum);
	}

	public SearchQuery previousPage() {
		this.pageNum--;
		
		return new SearchQuery(searchTerm, quality, genre, rating, sortBy, pageNum);
	}

	public String getQuality() {
		return "All".equals(quality.toString()) ? "0" : quality.toString();
	}

	public void setQuality(Quality quality) {
		this.quality = quality;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		if (rating >= 0 && rating < 10) {
			this.rating = rating;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		if ("".equals(searchTerm) || searchTerm == null) {
			this.searchTerm = "0";
		} else {
			this.searchTerm = searchTerm;
		}

	}

	public String getGenre() {
		return "All".equals(genre.toString()) ? "0" : genre.toString();
	}

	public void setGenre(Genre genre) {
		this.genre = genre;
	}

	public String getSortBy() {
		return sortBy.name().toLowerCase();
	}

	public void setSortBy(SortBy sortBy) {
		this.sortBy = sortBy;
	}

	public static SearchQuery getSearchQuery(String searchTerm, String quality, String genre, String rating,
			String sortBy, int pageNum) {
		Quality qualityEnum = Quality.toEnum(quality);
		Genre genreEnum = Genre.toEnum(genre);
		int ratingInt = 0;

		try {
			ratingInt = Integer.parseInt(Character.toString(rating.charAt(0)));
		} catch (NumberFormatException e) {
			ratingInt = 0;
		}

		SortBy sortByEnum = SortBy.toEnum(sortBy);

		return new SearchQuery(searchTerm, qualityEnum, genreEnum, ratingInt, sortByEnum, pageNum);
	}

	public static SearchQuery getDefaultSearchQuery() {
		return new SearchQuery("0", Quality.All, Genre.All, 0, SortBy.Date_Added, 1);

	}

}
