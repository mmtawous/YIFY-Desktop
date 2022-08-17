package yify.model.torrentclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;

import be.christophedetroyer.bencoding.Reader;
import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentFile;
import be.christophedetroyer.torrent.TorrentParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import yify.view.ui.TaskViewer;

public class TorrentClient {
	private static final String UNXI_EXEC_NAME = "/bin/zsh";
	private static final String UNIX_COMMAND_ARG = "-c";
	private static final String UNIX_ENV_PATH = "/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin";

	public static Process start(StreamType streamType, String torrentName, String downloadPath, Integer selectFileIndex,
			String torrentId, ObservableList<MovieFile> fileList) throws IOException {

		System.out.println("Started executing process");
		List<String> cmd = new ArrayList<String>();
		cmd.add(UNXI_EXEC_NAME);
		cmd.add(UNIX_COMMAND_ARG);

		String commandStr = "webtorrent ";

		if (streamType == StreamType.VLC) {
			commandStr += torrentId + " --vlc ";
		} else {
			commandStr += "download " + torrentId + " ";
		}

		if (downloadPath != null) {
			commandStr += "--out " + downloadPath + " ";
		} else {
			File tempFolder = Path.of(System.getProperty("java.io.tmpdir"), "YIFY-Desktop").toFile();

			// Won't work if file doesn't exist. Should work on the second run though.
			FileUtils.forceDeleteOnExit(tempFolder);

			String tempPath = tempFolder.getAbsolutePath();
			commandStr += "--out " + tempPath + " ";

		}

		if (selectFileIndex != null) {
			commandStr += "--select " + selectFileIndex.toString();
		}

		System.out.println(commandStr);

		cmd.add(commandStr);

		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.redirectErrorStream(true);
		pb.redirectOutput(Redirect.DISCARD);
		pb.environment().put("PATH", UNIX_ENV_PATH);

		String workingDir;
		// here, we assign the name of the OS, according to Java, to a variable...
		String OS = (System.getProperty("os.name")).toUpperCase();
		// to determine what the workingDirectory is.
		// if it is some version of Windows
		if (OS.contains("WIN")) {
			// it is simply the location of the "AppData" folder
			workingDir = System.getenv("AppData") + "\\YIFY-Desktop\\infoServerUrl.txt";
		} else if (OS.contains("MAC")) {
			workingDir = System.getProperty("user.home")
					+ "/Library/Application Support/YIFY-Desktop/infoServerUrl.txt";
		} else {
			workingDir = System.getProperty("user.home") + "/YIFY-Desktop/infoServerUrl.txt";
		}

		File fileToRead = new File(workingDir);
		if (fileToRead.exists()) {
			fileToRead.delete();
		}

		Process p = pb.start();
		System.out.println("Finished executing process");

		// This thread is mean to complete the process of waiting for the webtorrent-cli
		// to write the infoServerUrl to the designated file to be read IN THE
		// BACKGROUND while the TaskViewer initializes in the Application thread. If the
		// TaskViewer initializes before the infoServerUrl becomes available it will
		// wait until it is. As soon as the infoServerUrl String is ready it is
		// submitted
		// to the TaskViewer using the submitInfoServerUrl() static method.
		System.out.println("Started thread");
		ExecutorService executor = Executors.newSingleThreadExecutor();

		System.out.println("Started thread");
		executor.submit(new Runnable() {
			@Override
			public void run() {
				System.out.println("Started getting infoServerUrl");
				TaskViewer.submitInfoServerUrl(getInfoServerUrl(fileToRead), p);
				System.out.println("Finished getting infoServerUrl");

			}
		});
		System.out.println("Finished Starting thread");

		long downloadSize = 0;
		String fileName = null;
		if (selectFileIndex != null) {
			downloadSize = fileList.get(selectFileIndex).getFileSize();
			fileName = fileList.get(selectFileIndex).getFileName();
		} else {
			for (int i = 0; i < fileList.size(); i++) {
				downloadSize += fileList.get(i).getFileSize();
			}
		}

		System.out.println("Started adding task and opening GUI");
		if (streamType == StreamType.NONE)
			TaskViewer.addTask(torrentName, null, p, downloadSize, fileName);
		System.out.println("End adding task and opening GUI");

		return p;
	}

	public static void stop(Process p) {
		p.destroy();

	}

	public static ObservableList<MovieFile> getFileList(String torrentId) {

		// Temporarily save a file for CLI to process.
		File tempFile = null;
		try {
			URL torrentURL = new URL(torrentId);
			InputStream in = torrentURL.openStream();
			ReadableByteChannel rbc = Channels.newChannel(in);
			tempFile = File.createTempFile("temp", ".torrent");
			tempFile.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(tempFile);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

			fos.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Torrent torrentInfo = null;
		try {
			torrentInfo = TorrentParser.parseTorrent(new Reader(tempFile));
		} catch (IOException e) {
			System.err.println("Unable to read torrent file");
		} catch (ParseException e1) {
			// TODO Notification here for error parsing torrent file
			System.err.println("Unable to parse torrent file");
		}

		List<TorrentFile> fileList = torrentInfo.getFileList();
		ObservableList<MovieFile> result = FXCollections.observableArrayList();

		for (int i = 0; i < fileList.size(); i++) {
			String torrentName = torrentInfo.getName();

			List<String> fileDirs = fileList.get(i).getFileDirs();
			String fileName = fileDirs.get(fileDirs.size() - 1);

			long fileSize = fileList.get(i).getFileLength();

			result.add(new MovieFile(fileName, fileSize, torrentName, i));
		}

		return result;
	}

	private static String getInfoServerUrl(File fileToRead) {

		Scanner read = null;
		String infoServerUrl = "";
		// This purpose of this loop is to keep catching the exceptions that are thrown
		// until it stop throwing it. This happens because the cli script has not
		// written the infoServerUrl to the file by the time this method runs. This
		// ensures the quickest execution time, as opposed to sleeping for set amount of
		// time.
		while (true) {
			try {
				read = new Scanner(new FileInputStream(fileToRead));

				if (read.hasNextLine()) {
					infoServerUrl = read.nextLine();
					break;
				} else {
					continue;
				}
			} catch (FileNotFoundException e) {
				// Cry about it!
			}
		}

		read.close();
		fileToRead.delete();

		return infoServerUrl;

//		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//
//		// The second line should contain the following: Info server url: {url}
//		String rtrn = "";
//		try {
//			reader.readLine();
//			rtrn = reader.readLine().split("=")[1];
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return rtrn;
	}

//	/**
//	 * Used to read the stdout stream of Webtorrent-cli. This program uses this
//	 * method to read the torrent info of the specified torrent as a string.
//	 * 
//	 * @param process the process that will out the torrent info
//	 * @return a string representation of the process stdout stream
//	 */
//	private static String readInput(Process process) {
//		// looks like this method might have to go :/. Look at the drawTorrent function
//		// in the cli script. If you can find a way to make a JS script that handles all
//		// the torrenting stuff and gets the desired values then spits them into an html
//		// page at the loopback address with a random port in JSON format... you would
//		// be golden
//
//		// 20 hours later, we are golden :)
//		try (Scanner read = new Scanner(process.getInputStream()).useDelimiter("\\A")) {
//			if (read.hasNext()) {
//				String rtrn = read.next();
//				// System.out.println(rtrn);
//				read.close();
//				return rtrn;
//			} else {
//				read.close();
//				return "";
//			}
//		}
//
//	}

//	private static ObservableList<MovieFile> parseFileList(String input) {
//		ObservableList<MovieFile> result = FXCollections.observableArrayList();
//
//		JSONObject rawJSON = new JSONObject(input);
//		String torrentName = rawJSON.getString("name");
//		JSONArray files = rawJSON.getJSONArray("files");
//
//		for (int i = 0; i < files.length(); i++) {
//			JSONObject currentF = (JSONObject) files.get(i);
//			String fName = currentF.getString("name");
//			long fSize = currentF.getLong("length");
//
//			result.add(new MovieFile(fName, fSize, torrentName, i));
//		}
//
//		return result;
//	}
}
