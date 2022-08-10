package yify.model.moviecatalong.searchquery;

import java.util.Arrays;
import java.util.List;

public enum Genre {
	All, Action, Adventure, Animation, Biography, Comedy, Crime, Documentary, Drama, Family, Fantasy, Film_Noir,
	Game_Show, History, Horror, Music, Musical, Mystery, News, Reality_TV, Romance, Sci_Fi, Sport, Thriller, War,
	Western;

	@Override
	public String toString() {
		switch (this) {

		case Film_Noir:
			return "Film-Noir";
		case Sci_Fi:
			return "Sci-Fi";
		case Game_Show:
			return "Game-Show";
		case Reality_TV:
			return "Reality-TV";
		default:
			return name();
		}
	}

	public static List<String> getOptions() {
		String[] result = Arrays.stream(Genre.class.getEnumConstants()).map(Enum::toString).toArray(String[]::new);

		return Arrays.asList(result);
	}

	public static Genre toEnum(String str) {
		Genre[] genres = Genre.class.getEnumConstants();

		for (int i = 0; i < genres.length; i++) {
			if (str.equals(genres[i].toString())) {
				return genres[i];
			}
		}
		throw new IllegalArgumentException("The string passed was not found as a Genre enum.");
	}
}
