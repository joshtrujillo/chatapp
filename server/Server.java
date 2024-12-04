/**
 * Server class for the homework 4 chat app.
 *
 * @author Josh Trujillo
 */

import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class  Server
{
	public static final int DEFAULT_PORT = 8888;

	// construct a thread pool for concurrency	
	private static final Executor exec = Executors.newCachedThreadPool();

	public static void main(String[] args) throws IOException {
		ServerSocket sock = null;

		try {
			// establish the socket
			sock = new ServerSocket(DEFAULT_PORT);

			while (true) {
				/**
				 * now listen for connections
				 * and service the connection in a separate thread.
				 */
                ConcurrentHashMap<Socket, DataOutputStream> socketMap = new ConcurrentHashMap<>();

                
				Runnable task = new Connection(socketMap, sock.accept());
				exec.execute(task);
			}
		}
		catch (IOException ioe) { System.err.println(ioe); }
		finally {
			if (sock != null)
				sock.close();
		}
	}
}


