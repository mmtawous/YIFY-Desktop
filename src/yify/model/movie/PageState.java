package yify.model.movie;

import javafx.scene.layout.GridPane;

public class PageState {
	private GridPane movieGrid;
	private int pageLimit;
	private int movieCount;
	
	public PageState(GridPane movieGrid, int pageLimit, int movieCount) {
		this.movieGrid = movieGrid;
		this.pageLimit = pageLimit;
		this.movieCount = movieCount;
	}
	
	
	public GridPane getMovieGrid() {
		return movieGrid;
	}
	
	public int getPageLimit() {
		return pageLimit;
	}

	public int getMovieCount() {
		return movieCount;
	}
}
