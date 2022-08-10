package yify.model.torrentclient;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class SizeConvertCellFactory implements Callback<TableColumn<MovieFile, Long>, TableCell<MovieFile, Long>> {
	@Override
	public TableCell<MovieFile, Long> call(TableColumn<MovieFile, Long> column) {
		return new TableCell<MovieFile, Long>() {
			@Override
			public void updateItem(Long value, boolean empty) {
				super.updateItem(value, empty);
				if (empty || value == null) {
					setText(null);
				} else {
					setText(humanReadableByteCountSI(value));
				}
			}
		};
	}

	public static String humanReadableByteCountSI(long bytes) {
		if (-1000 < bytes && bytes < 1000) {
			return bytes + " B";
		}

		CharacterIterator ci = new StringCharacterIterator("kMGTPE");
		while (bytes <= -999_950 || bytes >= 999_950) {
			bytes /= 1000;
			ci.next();
		}
		return String.format("%.1f %cB", bytes / 1000.0, ci.current());
	}
}