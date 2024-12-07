/**
 * Server class for the homework 4 chat app.
 *
 * @author Josh Trujillo
 */
package cc.henhouse.chatapp.server;

import java.io.*;
import java.net.*;
import java.util.Vector;
import java.util.concurrent.*;

public class Server {
    public static final int DEFAULT_PORT = 8888;

    // construct a thread pool for concurrency
    private static final Executor exec = Executors.newCachedThreadPool();

    // shared resources fro all threads
    private static final ConcurrentHashMap<String, DataOutputStream> userMap =
            new ConcurrentHashMap<>();
    private static final Vector<String[]> messageList = new Vector<>();

    public static void main(String[] args) throws IOException {
        ServerSocket sock = null;

        try {
            // establish the socket
            sock = new ServerSocket(DEFAULT_PORT);

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
