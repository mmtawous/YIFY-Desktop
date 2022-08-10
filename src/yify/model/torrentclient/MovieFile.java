package yify.model.torrentclient;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MovieFile {
	private String torrentName;
	private int index;
	private StringProperty fileName;
	private LongProperty fileSize;

	public MovieFile(String fileName, long fileSize, String torrentName, int index) {
		setIndex(index);
		setTorrentName(torrentName);
		setFileName(fileName);
		setFileSize(fileSize);
	}

	public StringProperty fileNameProperty() {
		if (fileName == null)
			fileName = new SimpleStringProperty(this, "fileName");
		return fileName;
	}

	public LongProperty fileSizeProperty() {
		if (fileSize == null)
			fileSize = new SimpleLongProperty(this, "fileSize");
		return fileSize;
	}

	public void setFileName(String value) {
		fileNameProperty().set(value);
	}

	public String getFileName() {
		return fileNameProperty().get();
	}

	public void setFileSize(long value) {
		fileSizeProperty().set(value);
	}

	public long getFileSize() {
		return fileSizeProperty().get();
	}

	public void setTorrentName(String torrentName) {
		if (torrentName != null)
			this.torrentName = torrentName;
		else
			throw new NullPointerException();
	}

	public String getTorrentName() {
		return torrentName;
	}

	public void setIndex(int index) {
		if (index >= 0)
			this.index = index;
		else
			throw new IllegalArgumentException();
	}

	public int getIndex() {
		return index;
	}

}
