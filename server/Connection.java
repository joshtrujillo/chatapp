import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class Connection implements Runnable
{
	private Socket	client;
    private ArrayList<BufferedWriter> bufferedWriterList;
	private static Handler handler = new Handler();

	public Connection(ArrayList<BufferedWriter> bufferedWriterList, Socket client) {
		this.client = client;
        this.bufferedWriterList = bufferedWriterList;
	}

	/**
	 * This method runs in a separate thread.
	 */	
	public void run() { 
		try {
			handler.process(client);
		}
		catch (java.io.IOException ioe) {
			System.err.println(ioe);
		}
	}
}
