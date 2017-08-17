package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import utilities.Message;
import utilities.Observer;
import utilities.Subject;

public class ChatClient implements Runnable, Subject {

	private Socket server = null;
	private String host = null;
	private int port;
	private ObjectInputStream instream;
	private ObjectOutputStream outstream;
	private String name = null;
	private Message message;
	private boolean shutDown = false;
	private List<Observer> observers = new ArrayList<Observer>();

	public void shutDown() {
		shutDown = true;
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getNume() {
		return name;
	}

	 

	public ChatClient(String host, int port, String nume) {
		this.host = host;
		this.port = port;
		this.name = nume;
	}

	public synchronized boolean sendMessage(Message message) {
		boolean check = true;
		try {

			outstream.writeObject(message);
			outstream.flush();
		} catch (Exception e) {
			e.printStackTrace();
			check = false;
		}

		return check;
	}

	@Override
	public void run() {

		try {
		
			server = new Socket(host, port);
			System.out.println("Connected to the server");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			outstream = new ObjectOutputStream(server.getOutputStream());
			outstream.writeObject(new Message(3, name, 0, 0));
			outstream.flush();
			System.out.println("Message sent");
			instream = new ObjectInputStream(server.getInputStream());

			while (!shutDown) {
				message = (Message) instream.readObject();
				notifyAllObservers(message);
			}

			outstream.close();
			instream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addObserver(Observer ob) {
		observers.add(ob);
	}

	@Override
	public void deleteObserver(Observer ob) {
		observers.add(ob);
	}

	@Override
	public void notifyAllObservers(Message message) {
		for (Observer o : observers) {
			o.notify(message);
		}
	}
}