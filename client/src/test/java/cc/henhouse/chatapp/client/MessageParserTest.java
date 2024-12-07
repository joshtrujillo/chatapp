package cc.henhouse.chatapp.client;

import org.junit.jupiter.api.*;
import org.mockito.*;

class MessageParserTest {
    @Mock private ChatScreen mockScreen;

    @BeforeEach
    void setUp() {
        mockScreen = Mockito.mock(ChatScreen.class);
    }

    @Test
    void testHandleMessageAll() {
        String message = "MessageAll¤Alice¤Hello!";
        MessageParser.handleMessage(message, mockScreen);

        Mockito.verify(mockScreen).displayMessage("[All] Alice: Hello!");
    }

    @Test
    void testHandlePrivateMessage() {
        String message = "MessageIndividual¤Bob¤Hi Bob!";
        MessageParser.handleMessage(message, mockScreen);

        Mockito.verify(mockScreen).displayMessage("[Private] Bob: Hi Bob!");
    }

    @Test
    void testHandleJoinMessage() {
        String message = "Join¤Charlie";
        MessageParser.handleMessage(message, mockScreen);

        Mockito.verify(mockScreen).displayMessage("Charlie joined the chat.");
    }

    @Test
    void testHandleOnlineUsers() {
        String message = "OnlineUsers¤Alice,Bob,Charlie";
        MessageParser.handleMessage(message, mockScreen);

        Mockito.verify(mockScreen).displayMessage("Online users: Alice,Bob,Charlie");
    }

    @Test
    void testHandleError() {
        String message = "ERR¤Join¤Username already taken";
        MessageParser.handleMessage(message, mockScreen);

        Mockito.verify(mockScreen).displayMessage("[Error] Join: Username already taken");
        Mockito.verify(mockScreen).handleInvalidUsername();
    }

    @Test
    void testHandleUnknownMessage() {
        String message = "UnknownType¤Data";
        MessageParser.handleMessage(message, mockScreen);

        Mockito.verify(mockScreen).displayMessage("[Unknown] UnknownType¤Data");
    }
}
