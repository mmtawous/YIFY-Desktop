package yify.view.ui;

import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;

public class BufferBarPnl extends BorderPane {
	ProgressBar progressBar;
	
	public BufferBarPnl() {
		progressBar = new ProgressBar();
		progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
		progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setMaxHeight(10);
		this.setTop(progressBar);
	}
}
