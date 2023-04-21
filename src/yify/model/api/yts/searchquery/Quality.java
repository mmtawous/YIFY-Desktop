package yify.model.api.yts.searchquery;

import java.util.Arrays;
import java.util.List;

public enum Quality {
	All, Q480P, Q720P, Q1080P, Q2160P, Q3D;

	@Override
	public String toString() {
		switch (this) {
		case Q480P:
			return "480p";
		case Q720P:
			return "720p";
		case Q1080P:
			return "1080p";
		case Q2160P:
			return "2160p";
		case Q3D:
			return "3D";
		default:
			return name();
		}
	}

	public static List<String> getOptions() {
		String[] result = Arrays.stream(Quality.class.getEnumConstants()).map(Enum::toString).toArray(String[]::new);

		return Arrays.asList(result);
	}

	public static Quality toEnum(String str) {
		Quality[] qualities = Quality.class.getEnumConstants();
		
		for (int i = 0; i < qualities.length; i++) {
			if (str.equals(qualities[i].toString())) {
				return qualities[i];
			}
		}
		throw new IllegalArgumentException("The string passed was not found as a Quality enum.");
	}
}
