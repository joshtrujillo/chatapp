package cc.henhouse.chatapp.server;

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
            fromClient =
                    new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));

            // Handle join request
            String line = fromClient.readLine();
            String[] parts = line.split("¤");
            if (!parts[0].equals("Join") || parts.length < 2) {
                String response = "ERR¤Join¤Invalid join request\n";
                toClient.write(response.getBytes("UTF-8"));
                return;
            }

            username = parts[1];
            if (userMap.containsKey(username)) {
                String response = "ERR¤Join¤Username is not unique";
                toClient.write(response.getBytes("UTF-8"));
                return;
            }

            userMap.put(username, toClient);
            broadcastMessage(messageList, "Server", username + " has joined the chat!");

            while ((line = fromClient.readLine()) != null) {
                System.out.println("line: " + line);
                parts = line.split("¤");
                System.out.println("Request received:");
                for (String part : parts) System.out.println(part);
                switch (parts[0]) {
                    case "MessageAll":
                        broadcastMessage(messageList, username, parts[1]);
                        break;

                    case "MessageIndividual":
                        sendPrivateMessage(userMap, parts[1], username, parts[2]);
                        break;

                    case "viewOnlineUsers":
                        sendOnlineUsers(username, userMap);
                        break;

                    case "Leave":
                        userMap.remove(username);
                        broadcastMessage(messageList, "Server", username + " has left the chat.");
                        break;

                    default:
                        String response = "ERR¤Method¤Unsupported command\n";
                        toClient.write(response.getBytes("UTF-8"));
                }
            }
        } catch (IOException ioe) {
            System.err.println(ioe);
        } finally {
            // close streams and socket
            if (toClient != null) toClient.close();
        }
    }

    private void broadcastMessage(Vector<String[]> messageList, String sender, String message) {
        messageList.add(new String[] {sender, message});
    }

    private void sendPrivateMessage(
            ConcurrentHashMap<String, DataOutputStream> userMap,
            String recipient,
            String sender,
            String message)
            throws IOException {
        if (userMap.containsKey(recipient)) {
            String response = "MessageIndividual¤" + sender + "¤" + message + "\n";
            userMap.get(recipient).write(response.getBytes("UTF-8"));
        } else {
            String response = "ERR¤Message¤User " + recipient + " not found\n";
            userMap.get(sender).write(response.getBytes("UTF-8"));
        }
    }

    private void sendOnlineUsers(
            String username, ConcurrentHashMap<String, DataOutputStream> userMap)
            throws IOException {
        String users = String.join(",", userMap.keySet());
        String response = "OnlineUsers¤" + users + "\n";
        userMap.get(username).write(response.getBytes("UTF-8"));
    }
}
