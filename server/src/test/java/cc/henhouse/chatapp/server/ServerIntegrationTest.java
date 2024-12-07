package cc.henhouse.chatapp.server;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Executors;

class ServerIntegrationTest {

    @Test
    void testServerHandlesMultipleClients() throws Exception {
        Executors.newSingleThreadExecutor()
                .submit(
                        () -> {
                            try {
                                Server.main(new String[0]); // Start server
                            } catch (IOException e) {
                                fail("Server failed to start");
                            }
                        });

        // Simulate two clients
        Socket client1 = new Socket("localhost", Server.DEFAULT_PORT);
        Socket client2 = new Socket("localhost", Server.DEFAULT_PORT);

        PrintWriter out1 = new PrintWriter(client1.getOutputStream(), true);
        BufferedReader in1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));

        PrintWriter out2 = new PrintWriter(client2.getOutputStream(), true);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));

        // Client 1 joins
        out1.println("Join¤Alice");
        assertEquals("MessageAll¤Server¤Alice has joined the chat!", in2.readLine());

        // Client 2 joins
        out2.println("Join¤Bob");
        assertEquals("MessageAll¤Server¤Bob has joined the chat!", in1.readLine());

        // Client 1 sends a broadcast
        out1.println("MessageAll¤Hello, everyone!");
        assertEquals("MessageAll¤Alice¤Hello, everyone!", in2.readLine());
    }
}
