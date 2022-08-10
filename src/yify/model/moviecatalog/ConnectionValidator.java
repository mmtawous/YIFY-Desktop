package yify.model.moviecatalog;

import java.net.ConnectException;

/**
 * An interface representing a class that must keep checking its connection to a
 * server.
 * 
 * @author Mohamed Tawous
 *
 */
public interface ConnectionValidator {

	/**
	 * Checks that a connection between the client and the sever can be made.
	 * 
	 * @return a boolean representing the success or failure of the attempted
	 *         connection.
	 * @throws ConnectException 
	 */
	void checkConnection(String serverUrl) throws ConnectException;
}
