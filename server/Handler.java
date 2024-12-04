import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class Handler 
{
	/*
	 * this method is invoked by a separate thread
	 */
	public void process(ConcurrentHashMap<Socket, DataOutputStream> socketMap, Socket client) throws java.io.IOException {
		DataOutputStream toClient = null;

		try {
			toClient = new DataOutputStream(client.getOutputStream());

            socketMap.put(client, toClient);
            System.out.println("Client Connected!");
            System.out.println("Added socket and DataOutputStream to HashMap!");

			while (true) {
				
				try {
					Thread.sleep(5000);
				}
				catch (InterruptedException ie) { }
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
