package cc.henhouse.chatapp.server;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

class HandlerTest {
    private Handler handler;
    private ConcurrentHashMap<String, DataOutputStream> userMap;
    private BlockingQueue<String[]> messageQueue;
    private DataOutputStream mockStream;

    @BeforeEach
    void setUp() {
        handler = new Handler();
        userMap = new ConcurrentHashMap<>();
        messageQueue = mock(BlockingQueue.class);
        mockStream = mock(DataOutputStream.class);
    }

    @Test
    void testBroadcastMessage() throws InterruptedException {
        // Test that broadcastMessage adds a message to the queue
        handler.broadcastMessage(messageQueue, "Alice", "Hello, World!");
        verify(messageQueue).put(new String[] {"Alice", "Hello, World!"});
    }

    @Test
    void testSendPrivateMessage_Success() throws IOException {
        // Test private message is sent correctly
        userMap.put("Bob", mockStream);

        handler.sendPrivateMessage(userMap, "Bob", "Alice", "Hi, Bob!");

        verify(mockStream).write("MessageIndividual¤Alice¤Hi, Bob!\n".getBytes("UTF-8"));
    }

    @Test
    void testSendPrivateMessage_UserNotFound() throws IOException {
        // Test private message when recipient is not found
        userMap.put("Alice", mockStream);

        handler.sendPrivateMessage(userMap, "Charlie", "Alice", "Hi, Charlie!");

        verify(mockStream).write("ERR¤Message¤User Charlie not found\n".getBytes("UTF-8"));
    }
}
