/**
 * The BroadcastThread class handles broadcasting messages to all connected clients. It continuously
 * retrieves messages from a shared queue and sends them to all users except the sender.
 *
 * <p>Thread-Safe Design: This class uses a BlockingQueue to ensure thread safety when accessing and
 * processing messages concurrently.
 *
 * @author Josh Trujillo
 */
package cc.henhouse.chatapp.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class BroadcastThread implements Runnable {

    // The shared messageList for concurrent accesss.
    private BlockingQueue<String[]> messageList;
    // The shared map of usernames to their output streams for concurrent access.
    private ConcurrentHashMap<String, DataOutputStream> userMap;

    /**
     * BroadcastThread constructor.
     *
     * @param messageList The shared list of broadcast messages to be sent.
     * @param userMap The shared map of usernames to their output streams.
     */
    public BroadcastThread(
            BlockingQueue<String[]> messageList,
            ConcurrentHashMap<String, DataOutputStream> userMap) {
        this.messageList = messageList;
        this.userMap = userMap;
    }

    /**
     * Checks the messageList every 1/10 of a second. Sends the message to all online clients except
     * for the sender.
     */
    public void run() {
        while (true) {
            // sleep for 1/10th of a second
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {
            }
            if (messageList.isEmpty()) continue;
            for (String[] message : messageList) {
                String protocolMessage =
                        String.format("MessageAll¤%s¤%s\n", message[0], message[1]);
                for (String username : userMap.keySet()) {
                    if (username.equals(message[0])) continue; // skip sender
                    try {
                        userMap.get(username).write(protocolMessage.getBytes("UTF-8"));
                    } catch (IOException ioe) {
                        System.err.println("Error sending message to " + username);
                    }
                }
                messageList.remove(message);
            }
        }
    }
}
