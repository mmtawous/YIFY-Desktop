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
	private static final String TEMP_PATH = Path.of(System.getProperty("java.io.tmpdir"), "YIFY-Desktop").toString();
	private static final String UNXI_EXEC_NAME = "/bin/zsh";
	private static final String UNIX_COMMAND_ARG = "-c";
	private static final String UNIX_ENV_PATH = "/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin";
	private static final String RESOURCES_PATH = "/" + Path.of(System.getProperty("user.dir"), "resources").toString();
	private static final String NODE_PATH = "/"
			+ Path.of(RESOURCES_PATH, "node-v16.17.0-darwin-x64", "bin", "node").toString();
	private static final String WEBTORRENT_PATH = "/"
			+ Path.of(RESOURCES_PATH, "webtorrent-cli", "bin", "cmd.js").toString();
	private static ArrayList<Process> processList = new ArrayList<Process>();

	public static Process start(StreamType streamType, String torrentName, String downloadPath, Integer selectFileIndex,
			String torrentId, ObservableList<MovieFile> fileList) throws IOException {

		System.out.println("Started executing process");
		List<String> cmd = new ArrayList<String>();
		cmd.add(UNXI_EXEC_NAME);
		cmd.add(UNIX_COMMAND_ARG);

		String commandStr = NODE_PATH + " " + WEBTORRENT_PATH + " ";

		if (streamType == StreamType.VLC) {
			commandStr += torrentId + " --vlc ";
		} else if (streamType == StreamType.IINA) {
			commandStr += torrentId + " --iina ";
		} else if (streamType == StreamType.MPLAYER) {
			commandStr += torrentId + " --mplayer ";
		} else if (streamType == StreamType.MPV) {
			commandStr += torrentId + " --mpv ";
		} else if (streamType == StreamType.SMPLAYER) {
			commandStr += torrentId + " --smplayer ";
		} else {
			commandStr += "download " + torrentId + " ";
		}

		if (downloadPath != null) {
			// Adding quotes around download path to santize it. (Some file names have spaces so it
			// causes parse problems with webtorrent).
			commandStr += "--out " + "\"" + downloadPath + "\"" + " ";
		} else {
			commandStr += "--out " + TEMP_PATH + " ";
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

		// This thread is meant to complete the process of waiting for the
		// webtorrent-cli
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

		processList.add(p);

		return p;
	}
	
	public static int getNumRunningTasks() {
		int cnt = 0;

		if (processList != null) {
			for (int i = 0; i < processList.size(); i++) {
				if (processList.get(i).isAlive())
					cnt++;
			}
		}

		return cnt;
	}

	public static void quit() {
		File tempFile = new File(TEMP_PATH);
		if (tempFile.exists()) {
			try {
				FileUtils.deleteDirectory(tempFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for (int i = 0; i < processList.size(); i++) {
			stop(processList.get(i));
		}
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
	}
}
