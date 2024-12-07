package cc.henhouse.chatapp.server;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.DataOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

class BroadcastThreadTest {
    private BlockingQueue<String[]> messageQueue;
    private ConcurrentHashMap<String, DataOutputStream> userMap;
    private BroadcastThread broadcastThread;

    @BeforeEach
    void setUp() {
        messageQueue = mock(BlockingQueue.class);
        userMap = new ConcurrentHashMap<>();
        broadcastThread = new BroadcastThread(messageQueue, userMap);
    }

    @Test
    void testRun_SendsMessagesToUsers() throws Exception {
        // Set up mock users and messages
        DataOutputStream user1Stream = mock(DataOutputStream.class);
        DataOutputStream user2Stream = mock(DataOutputStream.class);
        userMap.put("Alice", user1Stream);
        userMap.put("Bob", user2Stream);

        when(messageQueue.take())
                .thenReturn(new String[] {"Alice", "Hello, everyone!"})
                .thenThrow(new InterruptedException());

        // Run the thread logic once
        new Thread(() -> broadcastThread.run()).start();
        Thread.sleep(200); // Allow thread to process message

        // Verify the message was sent to Bob but not Alice
        verify(user2Stream).write("MessageAll¤Alice¤Hello, everyone!\n".getBytes("UTF-8"));
        verify(user1Stream, never()).write(any());
    }
}
