/**
 * This thread is passed a socket that it reads from. Whenever it gets input it writes it to the
 * ChatScreen text area using the displayMessage() method.
 */
package cc.henhouse.chatapp.client;

import java.io.*;
import java.net.*;

public class ReaderThread implements Runnable {
    Socket server;
    BufferedReader fromServer;
    ChatScreen screen;

    public ReaderThread(Socket server, ChatScreen screen) {
        this.server = server;
        this.screen = screen;
    }

    public void run() {
        try {
            fromServer = new BufferedReader(new InputStreamReader(server.getInputStream()));

            while (true) {
                String message = fromServer.readLine();
                if (message == null) break;

                MessageParser.handleMessage(message, screen);
            }
        } catch (IOException ioe) {
            screen.displayMessage("[Error] Connection lost: " + ioe);
        }
    }
}
