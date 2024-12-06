/** Usage: java ChatScreen */
package cc.henhouse.chatapp.client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.*;

public class ChatScreen extends JFrame implements ActionListener, KeyListener {
    private JButton sendButton, exitButton, viewUsersButton;
    private JTextField sendText, recipientText;
    private JTextArea displayArea;
    private DataOutputStream toServer;

    public static final int PORT = 8888;

    public ChatScreen(Socket server) throws IOException {
        /* a panel used for placing components */
        JPanel inputPanel = new JPanel();
        JPanel controlPanel = new JPanel();

        /*
        Border etched = BorderFactory.createEtchedBorder();
        Border titled = BorderFactory.createTitledBorder(etched, "Enter Message Here ...");
        p.setBorder(titled);
        */

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

    private boolean validateUsername(String username) {
        String usernameRegex = "^[a-zA-Z0-9_]+$";
        if (!Pattern.matches(usernameRegex, username)) {
            displayMessage(
                    "[Error] Invalid username. Only letters, digits, and underscores are allowed.");
            return false;
        }
        return true;
    }

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

    /* Displays a message */
    public void displayMessage(String message) {
        displayArea.append(message + "\n");
    }

    /* This gets the text the user entered and outputs it in the display area. */
    public void displayText() {
        String message = sendText.getText().trim();
        StringBuffer buffer = new StringBuffer(message.length());

        // now reverse it
        for (int i = message.length() - 1; i >= 0; i--) buffer.append(message.charAt(i));

        displayArea.append(buffer.toString() + "\n");

        sendText.setText("");
        sendText.requestFocus();
    }

    public void sendMessageAll(String message) {
        try {
            toServer.writeBytes("MessageAll¤" + message + "\n");
        } catch (IOException ioe) {
            displayMessage("[Error] Could not send message: " + ioe.getMessage());
        }
    }

    public void sendPrivateMessage(String recipient, String message) {
        try {
            toServer.writeBytes("MessageIndividual¤" + recipient + "¤" + message + "\n");
        } catch (IOException ioe) {
            displayMessage("[Error] Could not send message: " + ioe.getMessage());
        }
    }

    /*
     * This method responds to action events .... i.e. button clicks and fulfills the contract of
     * the ActionListener interface.
     */
    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();

        if (source == sendButton) displayText();
        else if (source == exitButton) System.exit(0);
    }

    public void viewOnlineUsers() {
        try {
            toServer.writeBytes("viewOnlineUsers¤\n");
        } catch (IOException ioe) {
            displayMessage("[Error] Could not view users: " + ioe.getMessage());
        }
    }

    public void sendJoinMessage(String username) {
        try {
            if (!validateUsername(username)) {
                handleInvalidUsername();
                return;
            }

            toServer.writeBytes("Join¤" + username + "\n");
        } catch (IOException ioe) {
            displayMessage("[Error] Could not join: " + ioe.getMessage());
        }
    }

    public void sendLeaveMessage() {
        try {
            toServer.writeBytes("Leave¤\n");
        } catch (IOException ioe) {
            displayMessage("[Error] Could not leave: " + ioe.getMessage());
        }
    }

    /*
     * These methods responds to keystroke events and fulfills the contract of the KeyListener
     * interface.
     */

    /* This is invoked when the user presses the ENTER key. */
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) displayText();
    }

    /* Not implemented */
    public void keyReleased(KeyEvent e) {}

    /* Not implemented */
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        try {
            Socket server = new Socket(args[0], PORT);
            ChatScreen win = new ChatScreen(server);
            win.displayMessage("My name is " + args[1]);

            String username = JOptionPane.showInputDialog(win, "Enter your username:");
            if (username != null) {
                win.sendJoinMessage(username.trim());
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
