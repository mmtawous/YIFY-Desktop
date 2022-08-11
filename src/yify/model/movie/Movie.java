package yify.model.movie;

import javafx.scene.image.Image;

public class Movie {
	private int id;
	private Image thumbnail;
	private String title;
	private int year;
	private float rating;
	private String[] genres;
	private String lang;
	
	

	public Movie(int id, Image thumbnail, String title, int year, float rating, String[] genres, String lang) {
		this.id = id;
		this.thumbnail = thumbnail;
		this.title = title;
		this.rating = rating;
		this.genres = genres;
		this.year = year;
		this.lang = lang;
	}
	
	public int getId() {
		return id;
	}

	public Image getThumbnail() {
		return thumbnail;
	}

	public String getTitle() {
		return title;
	}
	
	public float getRating() {
		return rating;
	}
	
	public String[] getGenres() {
		return genres;
	}


	public int getYear() {
		return year;
	}

	public String getLang() {
		return lang;
	}	
	
}
