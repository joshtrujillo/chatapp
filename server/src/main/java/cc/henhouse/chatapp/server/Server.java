/**
 * The Server class handles creating the BroadcastThread and other Connections for each client.
 *
 * <p>Thread-safe: This class uses a BlockingQueue and ConcurrentHashMap for thread safety when
 * accesssing and processing messages concurrently.
 *
 * @author Josh Trujillo
 */
package cc.henhouse.chatapp.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {
    public static final int DEFAULT_PORT = 8888;

    // Construct a thread pool for concurrency
    private static final Executor exec = Executors.newCachedThreadPool();

    // Shared map of connected users: key is the username, value is the user's output stream.
    private static final ConcurrentHashMap<String, DataOutputStream> userMap =
            new ConcurrentHashMap<>();
    // Thread-safe queue for storing broadcast messages.
    private static final BlockingQueue<String[]> messageList = new LinkedBlockingQueue<>();

    public static void main(String[] args) throws IOException {
        ServerSocket sock = null;
        try {
            // Establish the socket
            sock = new ServerSocket(DEFAULT_PORT);
            Runnable broadcastThread = new BroadcastThread(messageList, userMap);
            exec.execute(broadcastThread);
            while (true) {
                /** now listen for connections and service the connection in a separate thread. */
                Runnable task = new Connection(messageList, userMap, sock.accept());
                exec.execute(task);
            }
        } catch (IOException ioe) {
            System.err.println(ioe);
        } finally {
            if (sock != null) sock.close();
        }
    }
}
