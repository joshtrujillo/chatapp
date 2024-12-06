import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Vector;

public class BroadcastThread implements Runnable {

    private Vector<String[]> messageList;
    private ConcurrentHashMap<String, DataOutputStream> userMap;

    public BroadcastThread(Vector<String[]> messageList, ConcurrentHashMap<String, DataOutputStream> userMap) {
        this.messageList = messageList;
        this.userMap = userMap;
    }

    public void run() {
        while (true) {
            // sleep for 1/10th of a second
            try { Thread.sleep(100); } catch (InterruptedException ignore) {}
            if (messageList.isEmpty())
                continue;

            for (String[] message : messageList) {
                for (String username : userMap.keySet()) {
                    String protocolMessage = String.format("MessageAll¤%s¤%s\n", message[0], message[1]);
                    try {
                        userMap.get(username).writeBytes(protocolMessage);
                    }
                    catch (IOException ioe) {}
                }
                messageList.remove(message);
            }
        }
    }

}
