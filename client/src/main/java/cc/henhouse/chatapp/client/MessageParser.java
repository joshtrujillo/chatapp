package cc.henhouse.chatapp.client;

public class MessageParser {

    public static void handleMessage(String message, ChatScreen screen) {
        String[] parts = message.split("¤");
        String type = parts[0];
        switch (type) {
            case "MessageAll":
                screen.displayMessage("[All] " + parts[1] + ": " + parts[2]);
                break;
            case "MessageIndividual":
                screen.displayMessage("[Private] " + parts[1] + ": " + parts[2]);
                break;
            case "Join":
                screen.displayMessage(parts[1] + " joined the chat.");
                break;
            case "OnlineUsers":
                screen.displayMessage("Online users: " + parts[1]);
                break;
            case "ERR":
                screen.displayMessage("[Error] " + parts[1] + ": " + parts[2]);
                if (parts[1].equals("Join")) {
                    screen.handleInvalidUsername();
                }
                break;
            default:
                screen.displayMessage("[Unknown] " + message);
        }
    }
}
