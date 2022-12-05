package yify.model.movie;

import javafx.scene.layout.GridPane;

public class PageState {
	GridPane movieGrid;
	int pageLimit;
	int movieCount;
	
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
