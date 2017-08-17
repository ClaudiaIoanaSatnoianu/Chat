package server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import utilities.Message;
import utilities.Observer;
import utilities.Subject;

public class ServerThread implements Runnable,Subject {
	private Socket connection = null;
	private ObjectInputStream objectInputStream = null;
	private ObjectOutputStream objectOutputStream = null;
	private final int id;
	private String name;
	private Message message = null;
	private  boolean  ready = false;
	private List<Observer> observers = new LinkedList<Observer>();
	
	public ServerThread(Socket newClient, int nr) {
		id = nr;
		connection = newClient;
	}

	public synchronized boolean send(Message message) {
		boolean check = true;

		try {
			objectOutputStream.writeObject(message);
			objectOutputStream.flush();
		} catch (Exception e) {
			notifyAllObservers(new Message(4, getName(), getId(), 0));
			System.out.println(id + ": The message could not be sent: " + e.toString());
			check = false;
		}

		return check;
	}

	boolean ready(){
		return ready;
	}
	
	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	@Override
	public void run() {
		try {
			System.out.println("Loading...");
			objectOutputStream = new ObjectOutputStream(connection.getOutputStream());
			objectInputStream = new ObjectInputStream(connection.getInputStream());
			System.out.println("Loading finished");
			message = (Message) objectInputStream.readObject();
			name = message.getText();
			notifyAllObservers(new Message(3, name, getId(), 0));
			notifyAllObservers(new Message(5, name, getId(), 0));
			
			while(true){
			message = (Message) objectInputStream.readObject();
			System.out.println("Received message: " + message.getText());
			notifyAllObservers(new Message(message.getType(), message.getText(), id, message.getIdTo()));
			}
			
		} catch (Exception e) {
				try{
				connection.close();
				}catch(Exception ex){
					System.out.println("The connection is off");
				}
				notifyAllObservers(new Message(4, getName(), getId(), 0));
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
		for (Observer observer : observers) {
			observer.notify(message);
		}
	}

}
