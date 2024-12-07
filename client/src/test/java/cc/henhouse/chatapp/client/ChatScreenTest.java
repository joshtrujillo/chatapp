package cc.henhouse.chatapp.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.*;

class ChatScreenTest {
    private Socket mockSocket;
    private DataOutputStream mockOutput;
    private ChatScreen chatScreen;

    @BeforeEach
    void setUp() throws IOException {
        mockSocket = mock(Socket.class);
        mockOutput = mock(DataOutputStream.class);

        when(mockSocket.getOutputStream()).thenReturn(mockOutput);

        chatScreen = new ChatScreen(mockSocket);
        chatScreen.setUsername("Alice");
    }

    @Test
    void testSendMessageAll() throws IOException {
        chatScreen.sendMessageAll("Hello, world!");
        verify(mockOutput).write("MessageAll¤Hello, world!\n".getBytes("UTF-8"));
    }

    @Test
    void testSendPrivateMessage() throws IOException {
        chatScreen.sendPrivateMessage("Bob", "Hi Bob!");
        verify(mockOutput).write("MessageIndividual¤Bob¤Hi Bob!\n".getBytes("UTF-8"));
    }

    @Test
    void testViewOnlineUsers() throws IOException {
        chatScreen.viewOnlineUsers();
        verify(mockOutput).write("viewOnlineUsers¤\n".getBytes("UTF-8"));
    }

    @Test
    void testSendLeaveMessage() throws IOException {
        chatScreen.sendLeaveMessage();
        verify(mockOutput).write("Leave\n".getBytes("UTF-8"));
    }

    @Test
    void testSendJoinMessage_ValidUsername() throws IOException {
        // Valid username
        chatScreen.sendJoinMessage("Alice");

        // Verify the server receives the join message
        verify(mockOutput).write("Join¤Alice\n".getBytes("UTF-8"));
    }

    @Test
    void testSendJoinMessage_InvalidUsername() {
        // Invalid username
        Exception exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            chatScreen.sendJoinMessage("Invalid Username!");
                        });

        // Verify the exception message
        assertEquals(
                "Username must only contain letters, numbers, and underscores",
                exception.getMessage());
    }
}
