/**
 * The Handler class provides all of the server side functionality for each client.
 *
 * <p>Thread safe: This class uses a shared BlockingQueue and ConcurrentHashMap for thread safety
 * when accessing and processing messages concurrently.
 *
 * @author Josh Trujillo
 */
package cc.henhouse.chatapp.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Handler {
    /**
     * Contains the main looping logic for adding clients to the userMap and handling client
     * requests.
     *
     * <p>Requests are parsed with String.split() and a switch statement.
     *
     * @param messageList The shared message queue for broadcast messages.
     * @param userMap The shared map of usernames to their output streams.
     * @param client The Socket for a specific client's connection.
     * @throws IOException If there is an issue getting the client's output stream.
     */
    public void process(
            BlockingQueue<String[]> messageList,
            ConcurrentHashMap<String, DataOutputStream> userMap,
            Socket client)
            throws IOException {
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
                parts = line.split("¤");
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

    /**
     * Sends a broadcast message to all connected clients/
     *
     * @param messageList The shared queue of messages to be sent.
     * @param sender The username of the sender.
     * @param message The message to be sent.
     */
    private void broadcastMessage(
            BlockingQueue<String[]> messageList, String sender, String message) {
        try {
            messageList.put(new String[] {sender, message});
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Sends a private message to a specific recipient.
     *
     * <p>If the recipient is not found in the current user map, the sender receives an error
     * message.
     *
     * @param userMap The shared map of usernames to their output streams.
     * @param recipient The username of the intended recipient.
     * @param sender The username of the sender.
     * @param message The mesasge to be send.
     * @throws IOException If there is an issue writing to the output stream.
     */
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

    /**
     * Sends a list of the online users to a specific recipient.
     *
     * <p>This method does not check if the username is present in the map.
     *
     * @param username The username of the recipient.
     * @param userMap The shared map of sernames to their output streams.
     * @throws IOException If there is an issue writing to the output stream.
     */
    private void sendOnlineUsers(
            String username, ConcurrentHashMap<String, DataOutputStream> userMap)
            throws IOException {
        String users = String.join(",", userMap.keySet());
        String response = "OnlineUsers¤" + users + "\n";
        userMap.get(username).write(response.getBytes("UTF-8"));
    }
}
