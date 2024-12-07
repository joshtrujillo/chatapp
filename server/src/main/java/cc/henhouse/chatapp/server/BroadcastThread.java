package cc.henhouse.chatapp.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class BroadcastThread implements Runnable {

    private Vector<String[]> messageList;
    private ConcurrentHashMap<String, DataOutputStream> userMap;

    public BroadcastThread(
            Vector<String[]> messageList, ConcurrentHashMap<String, DataOutputStream> userMap) {
        this.messageList = messageList;
        this.userMap = userMap;
    }

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
                System.out.println(protocolMessage);
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
