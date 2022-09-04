package yify.model.moviecatalog.searchquery;

import java.util.Arrays;
import java.util.List;

public enum SortBy {
	Date_Added, Rating, Title, Year, Peers, Seeds, Download_Count, Like_Count;

	@Override
	public String toString() {
		switch (this) {

		case Download_Count:
			return "Download Count";
		case Like_Count:
			return "Like Count";
		case Date_Added:
			return "Date Added";
		default:
			return name();

		}
	}

	public static List<String> getOptions() {
		String[] result = Arrays.stream(SortBy.class.getEnumConstants()).map(Enum::toString).toArray(String[]::new);

		return Arrays.asList(result);
	}

	public static SortBy toEnum(String str) {
		SortBy[] sortByEnums = SortBy.class.getEnumConstants();

		for (int i = 0; i < sortByEnums.length; i++) {
			if (str.equals(sortByEnums[i].toString())) {
				return sortByEnums[i];
			}
		}
		throw new IllegalArgumentException("The string passed was not found as a SortBy enum.");
	}

}
