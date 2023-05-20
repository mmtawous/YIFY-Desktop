package yify.view.ui;

import java.io.IOException;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import yify.model.api.yts.YTS_API;
import yify.model.api.yts.searchquery.SearchQuery;
import yify.view.ui.util.BackgroundWorker;

public class TitlePnl extends HBox {
	public TitlePnl() {
		/********************* Initialize titlePnl START ********************/
		this.setSpacing(950);
		this.setAlignment(Pos.CENTER_LEFT);
		this.setBackground(new Background(new BackgroundFill(Color.rgb(29, 29, 29, 1f), null, null)));
		this.setPadding(new Insets(10, 65, 10, 65));

		ImageView ytsLogo = new ImageView(new Image("File:assets/logo-YTS.png", 254, 80, true, true));
		ytsLogo.setFitWidth(127);
		ytsLogo.setFitHeight(40);
		HBox ytsLogoBox = new HBox(ytsLogo);

		ytsLogoBox.setOnMouseEntered(mouseEvent -> {
			final Animation animation = getFadeTransition(ytsLogo, 180, Interpolator.EASE_IN, true);
			animation.play();
		});

		ytsLogoBox.setOnMouseExited(mouseEvent -> {
			final Animation animation = getFadeTransition(ytsLogo, 180, Interpolator.EASE_OUT, false);
			animation.play();
		});

		ytsLogoBox.setOnMouseClicked(mouseEvent -> {

			BackgroundWorker.submit(() -> {
				App.showBufferBar();

				try {
					YTS_API.instance().makeRequest(SearchQuery.getDefaultSearchQuery());
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
				
				BrowserPnl.initNavBtns();
				App.switchSceneContent(null);
				App.hideBufferBar();
			});
		});

		this.getChildren().add(ytsLogoBox);

		ImageView taskViewerIcon = new ImageView(new Image("File:assets/taskManIcon.png"));

		Button taskViewerBtn = new Button("", taskViewerIcon);
		// hard coded insets for image size of icon
		taskViewerBtn.setBackground(
				new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, new Insets(9, 14, 9, 14))));
		taskViewerBtn.setFocusTraversable(false);
		taskViewerBtn.setTooltip(new Tooltip("View running tasks"));

		taskViewerBtn.setOnMouseEntered(moueseEvent -> {
			final Animation animation = getFadeTransition(taskViewerBtn, 180, Interpolator.EASE_IN, true);
			animation.play();

		});

		taskViewerBtn.setOnMouseExited(moueseEvent -> {
			final Animation animation = getFadeTransition(taskViewerBtn, 180, Interpolator.EASE_IN, false);
			animation.play();

		});

		taskViewerBtn.setOnMouseClicked(mouseEvent -> {
			TaskViewer.show();
		});

		this.getChildren().add(taskViewerBtn);

		this.setBorder(new Border(new BorderStroke(null, null, Color.rgb(47, 47, 47, 1f), null, null, null,
				BorderStrokeStyle.SOLID, null, CornerRadii.EMPTY, new BorderWidths(1), Insets.EMPTY)));
		/********************** Initialize titlePnl END *********************/
	}

	private Transition getFadeTransition(Node target, int cycleDuration, Interpolator interpolator, boolean fadeUp) {
		return new Transition() {

			{
				setCycleDuration(Duration.millis(cycleDuration));
				setInterpolator(interpolator);
			}

			@Override
			protected void interpolate(double frac) {
				if (fadeUp)
					target.setEffect(new DropShadow(5, 0, 0, Color.rgb(255, 255, 255, .40f * frac)));
				else
					target.setEffect(new DropShadow(5, 0, 0, Color.rgb(255, 255, 255, .40f * (1 - frac))));

			}

		};
	}
}
