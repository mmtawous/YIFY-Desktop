package yify.model.movie;

import javafx.scene.image.Image;

public class Movie {
	private final int id;
	private final Image thumbnail;
	private final String thumbnailUrl;
	private final String title;
	private final int year;
	private final float rating;
	private final String[] genres;
	private final String lang;

	public Movie(int id, Image thumbnail, String title, int year, float rating, String[] genres, String lang) {
		this.id = id;
		this.thumbnail = thumbnail;
		this.thumbnailUrl = null;
		this.title = title;
		this.rating = rating;
		this.genres = genres;
		this.year = year;
		this.lang = lang;
	}

	public Movie(int id, String thumbnailUrl, String title, int year, float rating, String[] genres, String lang) {
		this.id = id;
		this.thumbnail = null;
		this.thumbnailUrl = thumbnailUrl;
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

	public String getThumbnailUrl() {
		return thumbnailUrl;
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
