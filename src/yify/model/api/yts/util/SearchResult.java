package yify.model.api.yts.util;


public class SearchResult implements Comparable<SearchResult> {

	private String title;
	private Integer distance;

	public SearchResult(String title, Integer distance) {
		this.title = title;
		this.distance = distance;
	}

	public String getTitle() {
		return this.title;
	}

	public Integer getDistance() {
		return this.distance;
	}

	@Override
	public int compareTo(SearchResult o) {
		return this.distance.compareTo(o.getDistance());
	}

	@Override
	public String toString() {
		return title += ": Distance = " + distance;
	}

}
