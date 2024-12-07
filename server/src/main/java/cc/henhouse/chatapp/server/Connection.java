package cc.henhouse.chatapp.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Connection implements Runnable {
    private Socket client;
    private ConcurrentHashMap<String, DataOutputStream> userMap;
    private BlockingQueue<String[]> messageList;
    private static Handler handler = new Handler();

    public Connection(
            BlockingQueue<String[]> messageList,
            ConcurrentHashMap<String, DataOutputStream> userMap,
            Socket client) {
        this.client = client;
        this.userMap = userMap;
        this.messageList = messageList;
    }

    /** This method runs in a separate thread. */
    public void run() {
        try {
            handler.process(messageList, userMap, client);
        } catch (java.io.IOException ioe) {
            System.err.println(ioe);
        }
    }
}
