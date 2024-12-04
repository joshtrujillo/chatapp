import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class Connection implements Runnable
{
	private Socket	client;
    private ConcurrentHashMap<Socket, DataOutputStream> socketMap;
	private static Handler handler = new Handler();

	public Connection(ConcurrentHashMap<Socket, DataOutputStream> socketMap, Socket client) {
		this.client = client;
        this.socketMap = socketMap;
	}

	/**
	 * This method runs in a separate thread.
	 */	
	public void run() { 
		try {
			handler.process(socketMap, client);
		}
		catch (java.io.IOException ioe) {
			System.err.println(ioe);
		}
	}
}
