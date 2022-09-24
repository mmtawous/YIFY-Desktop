module yify.desktop {
	requires transitive javafx.graphics;
	requires javafx.controls;
	requires javafx.fxml;
	requires java.desktop;
	requires javafx.web;
	requires java.net.http;
	requires org.controlsfx.controls;
	requires torrent.parser;
	requires org.apache.commons.io;
	requires jdk.crypto.ec;
	requires jdk.crypto.cryptoki;
	requires com.google.gson;
	
	opens yify.view.ui to javafx.fxml;
	opens yify.model.torrentclient to javafx.base;
	
	exports yify.view.ui;
	
}