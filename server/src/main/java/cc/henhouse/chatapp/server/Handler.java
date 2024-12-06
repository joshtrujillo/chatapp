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

                toClient.writeBytes("ERR¤Join¤Invalid join request\n");
                return;
            }

            username = parts[1];
            if (userMap.containsKey(username)) {
                toClient.writeBytes("ERR¤Join¤Username is not unique");
                return;
            }

            userMap.put(username, toClient);
            broadcastMessage(messageList, "Join", username + " has joined the chat!");

            while (true) {
                line = fromClient.readLine();
                parts = line.split("¤");
                switch (parts[0]) {
                    case "MessageAll":
                        messageList.add(new String[] {username, parts[1]});
                        break;

                    case "MessageIndividual":
                        sendPrivateMessage(userMap, parts[1], username, parts[2]);
                        break;

                    case "viewOnlineUsers":
                        sendOnlineUsers(username, userMap);
                        break;

                    case "Leave":
                        userMap.remove(username);
                        broadcastMessage(messageList, "Leave", username + " has left the chat.");
                        break;

                    default:
                        toClient.writeBytes("ERR¤Method¤Unsupported command\n");
                }
            }
        } catch (IOException ioe) {
            System.err.println(ioe);
        } finally {
            // close streams and socket
            if (toClient != null) toClient.close();
        }
    }

    private void broadcastMessage(Vector<String[]> messageList, String type, String message) {
        messageList.add(new String[] {type, message});
    }

    private void sendPrivateMessage(
            ConcurrentHashMap<String, DataOutputStream> userMap,
            String recipient,
            String sender,
            String message)
            throws IOException {
        if (userMap.containsKey(recipient)) {
            userMap.get(recipient).writeBytes("MessageIndividual¤" + sender + "¤" + message + "\n");
        } else {
            userMap.get(sender).writeBytes("ERR¤Message¤User " + recipient + " not found\n");
        }
    }

    private void sendOnlineUsers(
            String username, ConcurrentHashMap<String, DataOutputStream> userMap)
            throws IOException {
        String users = String.join(",", userMap.keySet());
        userMap.get(username).writeBytes("OnlineUsers¤" + users + "\n");
    }
}
