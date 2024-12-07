/**
 * The ChatScreen class is the main class for the chatapp client.
 *
 * <p>JFrame UI shows user relevent information. Creates a ReaderThread to receive incoming server
 * responses.
 *
 * @author Josh Trujillo
 */
package cc.henhouse.chatapp.client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.regex.Pattern;

import javax.swing.*;

public class ChatScreen extends JFrame implements ActionListener, KeyListener {
    private JButton sendButton, exitButton, viewUsersButton;
    private JTextField sendText, recipientText;
    private JTextArea displayArea;
    private DataOutputStream toServer;
    private String username;

    public static final int PORT = 8888;

    public ChatScreen(Socket server) throws IOException {
        /* a panel used for placing components */
        JPanel inputPanel = new JPanel();
        JPanel controlPanel = new JPanel();

        /* set up all the components */
        sendText = new JTextField(30);
        recipientText = new JTextField(15);
        sendButton = new JButton("Send");
        exitButton = new JButton("Exit");
        viewUsersButton = new JButton("View Online Users");

        /* register the listeners for the different button clicks */
        sendText.addKeyListener(this);
        sendButton.addActionListener(this);
        exitButton.addActionListener(this);
        viewUsersButton.addActionListener(this);

        /* add the components to the input panel */
        inputPanel.add(new JLabel("Message: "));
        inputPanel.add(sendText);
        inputPanel.add(new JLabel("Recipient (optional): "));
        inputPanel.add(recipientText);
        inputPanel.add(sendButton);

        /* add the components to the control panel */
        controlPanel.add(viewUsersButton);
        controlPanel.add(exitButton);

        /* add panels to JFrame */
        getContentPane().add(inputPanel, BorderLayout.SOUTH);
        getContentPane().add(controlPanel, BorderLayout.NORTH);

        /*
         * add the text area for displaying output. Associate a scrollbar with this text area. Note
         * we add the scrollpane to the container, not the text area
         */
        displayArea = new JTextArea(15, 40);
        displayArea.setEditable(false);
        displayArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(displayArea);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        /* JFrame settings */
        setTitle("Chat Room");
        pack();
        setVisible(true);
        sendText.requestFocusInWindow();

        /* anonymous inner class to handle window closing events */
        addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent evt) {
                        ChatScreen.this.sendLeaveMessage();
                        System.exit(0);
                    }
                });
        toServer = new DataOutputStream(server.getOutputStream());
    }

    /**
     * Sets the users username.
     *
     * @param username The user's username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Validates the user's username.
     *
     * <p>Username can only be letters, numbers, and underscores. The username must also be unique
     * from other online users.
     *
     * @param username User's username to be validated.
     * @return Boolean Is the username valid or not?
     */
    private boolean validateUsername(String username) {
        String usernameRegex = "^[a-zA-Z0-9_]+$";
        if (!Pattern.matches(usernameRegex, username)) {
            displayMessage(
                    "[Error] Invalid username. Only letters, digits, and underscores are allowed.");
            return false;
        }
        return true;
    }

    /** Prompts the user for a new username if the previously entered username is not unique. */
    public void handleInvalidUsername() {
        SwingUtilities.invokeLater(
                () -> {
                    String newUsername =
                            JOptionPane.showInputDialog(
                                    this, "Username already taken. Enter a new username: ");
                    if (newUsername != null) {
                        sendJoinMessage(newUsername.trim());
                    } else {
                        System.exit(0);
                    }
                });
    }

    /** Displays a message on the ChatScreen. */
    public void displayMessage(String message) {
        displayArea.append(message + "\n");
    }

    /** This gets the text the user entered and outputs it in the display area. */
    public void displayText() {
        String message = sendText.getText().trim();
        displayMessage(message);
        sendText.setText("");
        sendText.requestFocusInWindow();
    }

    /**
     * Helper for sending messages.
     *
     * <p>Determinds whether to send a private message or broadcast messages based on the
     * recipientText field.
     */
    private void sendMessage() {
        String message = sendText.getText().trim();
        String recipient = recipientText.getText().trim();

        if (recipient.isEmpty()) {
            sendMessageAll(message);
        } else {
            sendPrivateMessage(recipient, message);
        }

        sendText.setText("");
        sendText.requestFocusInWindow();
    }

    /**
     * Sends a broadcast message to the server.
     *
     * @param message The message to be sent.
     */
    public void sendMessageAll(String message) {
        try {
            String request = "MessageAll¤" + message + "\n";
            toServer.write(request.getBytes("UTF-8"));
            displayMessage(username + ": " + message);
        } catch (IOException ioe) {
            displayMessage("[Error] Could not send message: " + ioe.getMessage());
        }
    }

    /**
     * Sends a private message to a recipient.
     *
     * @param recipient The recipient.
     * @param message The message to be sent.
     */
    public void sendPrivateMessage(String recipient, String message) {
        try {
            if (!message.isEmpty()) {
                String request = "MessageIndividual¤" + recipient + "¤" + message + "\n";
                toServer.write(request.getBytes("UTF-8"));
                displayMessage("[To " + recipient + "] " + username + ": " + message);
            } else {
                displayMessage("[Error] Type message to send first.");
            }
        } catch (IOException ioe) {
            displayMessage("[Error] Could not send message: " + ioe.getMessage());
        }
    }

    /**
     * Requests a list of online users from the server.
     *
     * <p>The response is handled by the ReaderThread.
     */
    public void viewOnlineUsers() {
        try {
            String request = "viewOnlineUsers¤\n";
            toServer.write(request.getBytes("UTF-8"));
        } catch (IOException ioe) {
            displayMessage("[Error] Could not view users: " + ioe.getMessage());
        }
    }

    /**
     * Tells the server the client is joining.
     *
     * <p>The username is validated by helper methods and the user is prompted for a new username if
     * needed.
     *
     * @param username The username to join the chat room with.
     */
    public void sendJoinMessage(String username) {
        try {
            if (!validateUsername(username)) {
                handleInvalidUsername();
                return;
            }
            String request = "Join¤" + username + "\n";
            toServer.write(request.getBytes("UTF-8"));
        } catch (IOException ioe) {
            displayMessage("[Error] Could not join: " + ioe.getMessage());
        }
    }

    /**
     * Tells the server the client is leaving.
     *
     * <p>The server notifies other users of this users departure.
     */
    public void sendLeaveMessage() {
        try {
            String request = "Leave\n";
            toServer.write(request.getBytes("UTF-8"));
        } catch (IOException ioe) {
            displayMessage("[Error] Could not leave: " + ioe.getMessage());
        }
    }

    /**
     * This method responds to action events .... i.e. button clicks and fulfills the contract of
     * the ActionListener interface.
     */
    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();

        if (source == sendButton) {
            sendMessage();
        } else if (source == viewUsersButton) {
            viewOnlineUsers();
        } else if (source == exitButton) {
            sendLeaveMessage();
            System.exit(0);
        }
    }

    /** Invoked when the user presses the ENTER key. Calls the sendMessage helper. */
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) sendMessage();
    }

    /* Not implemented */
    public void keyReleased(KeyEvent e) {}

    /* Not implemented */
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        try {
            Socket server = new Socket(args[0], PORT);
            ChatScreen win = new ChatScreen(server);

            String username = JOptionPane.showInputDialog(win, "Enter your username:");
            if (username != null) {
                win.setUsername(username.trim());
                win.sendJoinMessage(username.trim());
                win.displayMessage("Welcome " + username.trim() + "\n");
                win.viewOnlineUsers();
            } else {
                System.exit(0); // Exit if no username is provided
            }
            Thread readerThread = new Thread(new ReaderThread(server, win));
            readerThread.start();
        } catch (UnknownHostException uhe) {
            System.err.println("[Error] Unable to connect: " + uhe.getMessage());
        } catch (IOException ioe) {
            System.err.println("[Error] Unable to connect: " + ioe.getMessage());
        }
    }
}
