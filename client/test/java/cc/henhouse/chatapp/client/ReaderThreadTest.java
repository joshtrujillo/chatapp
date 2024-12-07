package cc.henhouse.chatapp.client;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.*;

class ReaderThreadTest {
    private Socket mockSocket;
    private ChatScreen mockScreen;
    private BufferedReader mockInput;

    @BeforeEach
    void setUp() throws IOException {
        mockSocket = mock(Socket.class);
        mockScreen = mock(ChatScreen.class);
        mockInput = mock(BufferedReader.class);

        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
        when(mockSocket.getInputStream())
                .thenReturn(new ByteArrayInputStream("MessageAll¤Alice¤Hello\n".getBytes()));
    }

    @Test
    void testRun() throws IOException {
        ReaderThread readerThread = new ReaderThread(mockSocket, mockScreen);
        new Thread(readerThread).start();
        verify(mockScreen).displayMessage(any());
    }
}
