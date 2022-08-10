package yify.model.moviecatalog;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A thread that checks the connection between the client and the server at the
 * specified time interval in seconds.
 * 
 * @author Mohamed Tawous
 *
 */
public class ConnectionThread implements ConnectionValidator {
	public static final String PROBE_URL = "https://yts.mx";
	private Timer timer;

	public ConnectionThread(int seconds) {
		timer = new Timer();
		timer.scheduleAtFixedRate(new CheckConnectionTask(), 0, seconds * 1000);
	}

	/**
	 * Checks that a connection between the client and the sever can be made. If a
	 * connection can not be made a ConnectException is thrown.
	 * 
	 * @throws ConnectException if connection to https://yts.mx could not be
	 *                          established
	 */
	@Override
	public void checkConnection(String serverUrl) throws ConnectException {
		int statusCode = 0;
		try {
			URL url = new URL(serverUrl);
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setConnectTimeout(10000);
			statusCode = http.getResponseCode();
		} catch (Exception e) {
			System.out.println(statusCode);
			throw new ConnectException("Unable to connect to the YTS.MX!");
		}
		if (statusCode != 200)
			throw new ConnectException("Unable to connect to the YTS.MX!");
		else
			MovieCatalog.setConnectionOkay(true);
	}

	public void kill() {
		timer.cancel();
	}

	private class CheckConnectionTask extends TimerTask {
		public void run() {
			try {
				checkConnection(PROBE_URL);
			} catch (ConnectException e) {
				MovieCatalog.setConnectionOkay(false);
			}

		}
	}

}
