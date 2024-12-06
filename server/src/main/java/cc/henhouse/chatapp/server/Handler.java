import java.io.*;
import java.net.*;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Handler {
    /*
     * this method is invoked by a separate thread
     */
    public void process(
            Vector<String[]> messageList,
            ConcurrentHashMap<String, DataOutputStream> userMap,
            Socket client)
            throws java.io.IOException {
        DataOutputStream toClient = null;
        BufferedReader fromClient = null;
        String username = null;

        try {
            toClient = new DataOutputStream(client.getOutputStream());
            fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));

            // Check for unique username on client connection
            String line = fromClient.readLine();
            String[] parts = line.split("¤");
            if (userMap.containsKey(parts[1])) {
                String error = "ERR¤Join¤Non unique username\n";
                toClient.writeBytes(error);
                toClient.close();
            }

            username = parts[1];

            userMap.put(username, toClient);
            // Add OK response?
            System.out.println("Client Connected!");
            System.out.println("Added socket and DataOutputStream to HashMap!");

            while (true) {
                line = fromClient.readLine();
                parts = line.split("¤");
                switch (parts[0]) {
                    case "MessageAll":
                        messageList.add(new String[] {username, parts[1]});
                        break;
                    case "MessageIndividual":
                        // TODO
                        break;
                    case "viewOnlineUsers":
                        // TODO
                        break;
                    case "Leave":
                        // TODO
                        break;
                    default:
                        // TODO
                        // send error with type Method.
                }
            }
        } catch (IOException ioe) {
            System.err.println(ioe);
        } finally {
            // close streams and socket
            if (toClient != null) toClient.close();
        }
    }
}
