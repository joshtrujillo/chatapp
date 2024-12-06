import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Vector;

public class Handler 
{
	/*
	 * this method is invoked by a separate thread
	 */
	public void process(Vector<String> messageList, ConcurrentHashMap<String, DataOutputStream> userMap, Socket client) throws java.io.IOException {
		DataOutputStream toClient = null;
        BufferedReader fromClient = null;

		try {
			toClient = new DataOutputStream(client.getOutputStream());
			fromClient = new BufferedReader(new InputStreamReader (client.getInputStream()));

            // Check for unique username on client connection
            String line = fromClient.readLine();
            String[] parts = line.split("¤");
            if (userMap.containsKey(parts[1])) {
                String error = "ERR¤Join¤Non unique username\n";
                toClient.writeBytes(error);
                toClient.close();
            };

            userMap.put(parts[1], toClient);
            System.out.println("Client Connected!");
            System.out.println("Added socket and DataOutputStream to HashMap!");

			while (true) {
                line = fromClient.readLine();

            }
        }
		catch (IOException ioe) {
			System.err.println(ioe);
		}
		finally {
			// close streams and socket
			if (toClient != null)
				toClient.close();
		}
	}
}
